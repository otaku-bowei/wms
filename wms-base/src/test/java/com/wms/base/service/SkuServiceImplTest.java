package com.wms.base.service;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.api.system.client.CodeRuleFeignClient;
import com.wms.base.domain.dto.SkuCreateDTO;
import com.wms.base.domain.entity.BaseSku;
import com.wms.base.domain.query.SkuQuery;
import com.wms.base.mapper.BaseCategoryMapper;
import com.wms.base.mapper.BaseSkuBarcodeMapper;
import com.wms.base.mapper.BaseSkuMapper;
import com.wms.base.service.impl.SkuServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.base.domain.vo.SkuVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SKU 服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.3.1-01 ~ TP-R1-1.3.2-08
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class SkuServiceImplTest {

    @Mock
    private BaseSkuMapper skuMapper;

    @Mock
    private BaseSkuBarcodeMapper barcodeMapper;

    @Mock
    private BaseCategoryMapper categoryMapper;

    @Mock
    private CodeRuleFeignClient codeRuleFeignClient;

    private SkuServiceImpl skuService;

    @BeforeEach
    void setUp() {
        skuService = new SkuServiceImpl(barcodeMapper, categoryMapper, codeRuleFeignClient);
        ReflectionTestUtils.setField(skuService, "baseMapper", skuMapper);
    }

    @Test
    @DisplayName("新增 SKU 成功：保存主表与条形码")
    void createSku_shouldSaveSkuAndBarcodes() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(barcodeMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        skuService.createSku(buildCreateDTO("SP001", List.of("6901234567890")));

        verify(skuMapper).insert(any(BaseSku.class));
        verify(barcodeMapper).insert(any());
    }

    @Test
    @DisplayName("SPU+颜色+尺码 组合已存在：抛出业务异常")
    void createSku_shouldThrow_whenCombinationDuplicated() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> skuService.createSku(buildCreateDTO("SP001", List.of("6901234567890"))));

        assertThat(ex.getMessage()).contains("组合已存在");
    }

    @Test
    @DisplayName("SKU 编码已存在：抛出 20001")
    void createSku_shouldThrow_whenSkuCodeExists() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> skuService.createSku(buildCreateDTO("SP001", List.of("6901234567890"))));

        assertThat(ex.getCode()).isEqualTo(20001);
    }

    @Test
    @DisplayName("条形码被占用：抛出 20002")
    void createSku_shouldThrow_whenBarcodeOccupied() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(barcodeMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> skuService.createSku(buildCreateDTO("SP001", List.of("6901234567890"))));

        assertThat(ex.getCode()).isEqualTo(20002);
    }

    @Test
    @DisplayName("条形码为空：抛出 400")
    void createSku_shouldThrow_whenBarcodeEmpty() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        BizException ex = assertThrows(BizException.class,
                () -> skuService.createSku(buildCreateDTO("SP001", List.of())));

        assertThat(ex.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("编码服务不可用时：使用本地兜底生成编码")
    void createSku_shouldFallbackCode_whenFeignFailed() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(codeRuleFeignClient.nextCode("SKU")).thenThrow(new RuntimeException("feign error"));
        when(barcodeMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        skuService.createSku(buildCreateDTO(null, List.of("6901234567890")));

        verify(skuMapper).insert(any(BaseSku.class));
    }

    @Test
    @DisplayName("分页查询：返回 SKU 视图并补全条形码")
    @SuppressWarnings("unchecked")
    void page_shouldReturnSkuVO() {
        BaseSku sku = new BaseSku();
        sku.setId(1L);
        sku.setSkuCode("SP001");
        sku.setSkuName("夏季连衣裙");
        Page<BaseSku> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of(sku));
        mockPage.setTotal(1);
        when(skuMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(mockPage);
        com.wms.base.domain.entity.BaseSkuBarcode barcode = new com.wms.base.domain.entity.BaseSkuBarcode();
        barcode.setBarcode("6901234567890");
        when(barcodeMapper.selectList(any(Wrapper.class))).thenReturn(List.of(barcode));

        PageResult<SkuVO> result = skuService.page(new SkuQuery());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList().get(0).getBarcodes()).containsExactly("6901234567890");
    }

    @Test
    @DisplayName("批量导入：解析 Excel 并逐行创建")
    void importSkus_shouldParseAndCreate() {
        when(skuMapper.selectCount(any(Wrapper.class))).thenReturn(0L);
        when(barcodeMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        List<List<String>> head = List.of(List.of("SKU编码"), List.of("SPU编码"), List.of("商品名称"),
                List.of("颜色"), List.of("尺码"), List.of("条形码"));
        List<List<Object>> data = List.of(
                List.of("SP001", "SPU001", "夏季连衣裙", "红色", "S", "6901234567890"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        EasyExcel.write(out).head(head).sheet("Sheet1").doWrite(data);

        Map<String, Object> result = skuService.importSkus(new ByteArrayInputStream(out.toByteArray()));

        assertThat(result.get("totalCount")).isEqualTo(1);
        assertThat(result.get("successCount")).isEqualTo(1);
        assertThat(result.get("failCount")).isEqualTo(0);
    }

    @Test
    @DisplayName("启用/停用 SKU：更新状态")
    void changeStatus_shouldUpdate() {
        when(skuMapper.selectById(1L)).thenReturn(new BaseSku());

        skuService.changeStatus(1L, 0);

        verify(skuMapper).updateById(any(BaseSku.class));
    }

    @Test
    @DisplayName("删除 SKU：级联删除条形码")
    void deleteSku_shouldDeleteBarcodes() {
        when(skuMapper.selectById(1L)).thenReturn(new BaseSku());

        skuService.deleteSku(1L);

        verify(skuMapper).deleteById(1L);
        verify(barcodeMapper).delete(any(Wrapper.class));
    }

    @Test
    @DisplayName("查询详情：SKU 不存在时返回 null")
    void getById_shouldReturnNull_whenNotExists() {
        when(skuMapper.selectById(1L)).thenReturn(null);

        assertThat(skuService.getById(1L)).isNull();
    }

    private SkuCreateDTO buildCreateDTO(String skuCode, List<String> barcodes) {
        SkuCreateDTO dto = new SkuCreateDTO();
        dto.setSkuCode(skuCode);
        dto.setSpuCode("SPU001");
        dto.setSkuName("夏季连衣裙");
        dto.setColor("红色");
        dto.setSize("S");
        dto.setWeight(new BigDecimal("200"));
        dto.setBarcodes(barcodes);
        dto.setStatus(1);
        return dto;
    }
}
