package com.wms.base.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.wms.base.domain.dto.WarehouseDTO;
import com.wms.base.domain.dto.ZoneDTO;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.BaseZoneMapper;
import com.wms.base.service.impl.WarehouseServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.PageResult;
import com.wms.base.domain.query.WarehouseQuery;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 仓库与区域服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.4.1-01 ~ TP-R1-1.4.3-07
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private BaseWarehouseMapper warehouseMapper;

    @Mock
    private BaseZoneMapper zoneMapper;

    private WarehouseServiceImpl warehouseService;

    @BeforeEach
    void setUp() {
        warehouseService = new WarehouseServiceImpl(zoneMapper);
        ReflectionTestUtils.setField(warehouseService, "baseMapper", warehouseMapper);
    }

    @Test
    @DisplayName("新增仓库成功：保存实体")
    void createWarehouse_shouldSave() {
        when(warehouseMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        warehouseService.createWarehouse(buildWarehouseDTO("WH-002"));

        verify(warehouseMapper).insert(any(BaseWarehouse.class));
    }

    @Test
    @DisplayName("仓库编码已存在：抛出 30001")
    void createWarehouse_shouldThrow_whenCodeExists() {
        when(warehouseMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> warehouseService.createWarehouse(buildWarehouseDTO("WH-001")));

        assertThat(ex.getCode()).isEqualTo(30001);
    }

    @Test
    @DisplayName("仓库编码为空：抛出 400")
    void createWarehouse_shouldThrow_whenCodeEmpty() {
        BizException ex = assertThrows(BizException.class,
                () -> warehouseService.createWarehouse(buildWarehouseDTO("")));

        assertThat(ex.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("修改不存在的仓库：抛出 30004")
    void updateWarehouse_shouldThrow_whenNotExists() {
        when(warehouseMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> warehouseService.updateWarehouse(1L, buildWarehouseDTO("WH-001")));

        assertThat(ex.getCode()).isEqualTo(30004);
    }

    @Test
    @DisplayName("修改仓库：编码不可变更")
    void updateWarehouse_shouldKeepCode() {
        BaseWarehouse existing = new BaseWarehouse();
        existing.setId(1L);
        existing.setWarehouseCode("WH-001");
        when(warehouseMapper.selectById(1L)).thenReturn(existing);

        warehouseService.updateWarehouse(1L, buildWarehouseDTO("WH-999"));

        verify(warehouseMapper).updateById(any(BaseWarehouse.class));
        assertThat(existing.getWarehouseCode()).isEqualTo("WH-001");
    }

    @Test
    @DisplayName("启用/停用仓库：更新状态")
    void changeStatus_shouldUpdate() {
        when(warehouseMapper.selectById(1L)).thenReturn(new BaseWarehouse());

        warehouseService.changeStatus(1L, 0);

        verify(warehouseMapper).updateById(any(BaseWarehouse.class));
    }

    @Test
    @DisplayName("新增区域成功：同仓库下编码唯一")
    void createZone_shouldSave() {
        when(warehouseMapper.selectById(1L)).thenReturn(new BaseWarehouse());
        when(zoneMapper.selectCount(any(Wrapper.class))).thenReturn(0L);

        warehouseService.createZone(1L, buildZoneDTO("B"));

        verify(zoneMapper).insert(any(BaseZone.class));
    }

    @Test
    @DisplayName("区域编码重复：抛出 30003")
    void createZone_shouldThrow_whenCodeExists() {
        when(warehouseMapper.selectById(1L)).thenReturn(new BaseWarehouse());
        when(zoneMapper.selectCount(any(Wrapper.class))).thenReturn(1L);

        BizException ex = assertThrows(BizException.class,
                () -> warehouseService.createZone(1L, buildZoneDTO("A")));

        assertThat(ex.getCode()).isEqualTo(30003);
    }

    @Test
    @DisplayName("仓库不存在：新增区域抛出 30004")
    void createZone_shouldThrow_whenWarehouseNotExists() {
        when(warehouseMapper.selectById(1L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> warehouseService.createZone(1L, buildZoneDTO("A")));

        assertThat(ex.getCode()).isEqualTo(30004);
    }

    @Test
    @DisplayName("查询启用仓库下拉选项")
    void listEnabled_shouldReturnEnabled() {
        when(warehouseMapper.selectList(any(Wrapper.class))).thenReturn(List.of(new BaseWarehouse()));

        assertThat(warehouseService.listEnabled()).hasSize(1);
    }

    @Test
    @DisplayName("分页查询仓库")
    @SuppressWarnings("unchecked")
    void page_shouldReturnPageResult() {
        Page<BaseWarehouse> mockPage = new Page<>(1, 20);
        mockPage.setRecords(List.of(new BaseWarehouse()));
        mockPage.setTotal(1);
        when(warehouseMapper.selectPage(any(IPage.class), any(Wrapper.class))).thenReturn(mockPage);

        PageResult<BaseWarehouse> result = warehouseService.page(new WarehouseQuery());

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getList()).hasSize(1);
    }

    private WarehouseDTO buildWarehouseDTO(String code) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setWarehouseCode(code);
        dto.setWarehouseName("测试仓库");
        dto.setWarehouseType("REGIONAL");
        dto.setRegion("华南");
        dto.setStatus(1);
        return dto;
    }

    private ZoneDTO buildZoneDTO(String zoneCode) {
        ZoneDTO dto = new ZoneDTO();
        dto.setZoneCode(zoneCode);
        dto.setZoneName(zoneCode + " 区");
        dto.setZoneType("STORAGE");
        dto.setStatus(1);
        return dto;
    }
}
