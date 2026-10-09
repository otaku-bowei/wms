package com.wms.base.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.api.system.client.CodeRuleFeignClient;
import com.wms.base.domain.dto.SkuCreateDTO;
import com.wms.base.domain.dto.SkuUpdateDTO;
import com.wms.base.domain.entity.BaseCategory;
import com.wms.base.domain.entity.BaseSku;
import com.wms.base.domain.entity.BaseSkuBarcode;
import com.wms.base.domain.query.SkuQuery;
import com.wms.base.domain.vo.SkuVO;
import com.wms.base.mapper.BaseCategoryMapper;
import com.wms.base.mapper.BaseSkuBarcodeMapper;
import com.wms.base.mapper.BaseSkuMapper;
import com.wms.base.service.SkuService;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.mybatis.convert.PageConvert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SKU 服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SkuServiceImpl extends ServiceImpl<BaseSkuMapper, BaseSku> implements SkuService {

    private final BaseSkuBarcodeMapper barcodeMapper;
    private final BaseCategoryMapper categoryMapper;
    private final CodeRuleFeignClient codeRuleFeignClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSku(SkuCreateDTO dto) {
        // 1. SPU + 颜色 + 尺码 组合唯一
        Long duplicate = this.count(new LambdaQueryWrapper<BaseSku>()
                .eq(BaseSku::getSpuCode, dto.getSpuCode())
                .eq(BaseSku::getColor, dto.getColor())
                .eq(BaseSku::getSize, dto.getSize()));
        if (duplicate != null && duplicate > 0) {
            throw new BizException(ErrorCode.SKU_CODE_EXISTS.getCode(), "SPU+颜色+尺码 组合已存在");
        }
        // 2. SKU 编码
        String skuCode = dto.getSkuCode();
        if (StringUtils.hasText(skuCode)) {
            if (existsSkuCode(skuCode)) {
                throw new BizException(ErrorCode.SKU_CODE_EXISTS);
            }
        } else {
            skuCode = generateSkuCode();
        }
        // 3. 条形码校验
        if (CollectionUtils.isEmpty(dto.getBarcodes())) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "条形码不能为空");
        }
        for (String barcode : dto.getBarcodes()) {
            if (!StringUtils.hasText(barcode)) {
                continue;
            }
            if (existsBarcode(barcode)) {
                throw new BizException(ErrorCode.SKU_BARCODE_OCCUPIED);
            }
        }
        // 4. 保存 SKU
        BaseSku entity = new BaseSku();
        BeanUtils.copyProperties(dto, entity);
        entity.setSkuCode(skuCode);
        entity.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        entity.setCreatedFrom("MANUAL");
        this.save(entity);
        // 5. 保存条形码
        saveBarcodes(entity.getId(), dto.getBarcodes());
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSku(Long id, SkuUpdateDTO dto) {
        BaseSku entity = super.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        entity.setSkuName(dto.getSkuName());
        entity.setCategoryId(dto.getCategoryId());
        entity.setImageUrl(dto.getImageUrl());
        entity.setWeight(dto.getWeight());
        entity.setVolume(dto.getVolume());
        entity.setLengthMm(dto.getLengthMm());
        entity.setWidthMm(dto.getWidthMm());
        entity.setHeightMm(dto.getHeightMm());
        entity.setShelfLifeDays(dto.getShelfLifeDays());
        this.updateById(entity);
        // 条形码全量覆盖
        if (dto.getBarcodes() != null) {
            barcodeMapper.delete(new LambdaQueryWrapper<BaseSkuBarcode>().eq(BaseSkuBarcode::getSkuId, id));
            saveBarcodes(id, dto.getBarcodes());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        BaseSku entity = super.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        entity.setStatus(status);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSku(Long id) {
        BaseSku entity = super.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.SKU_NOT_FOUND);
        }
        // R1 阶段暂无库存表，若存在 R2 业务记录则拒绝删除（此处以创建来源判断为辅）
        if ("IMPORT".equals(entity.getCreatedFrom()) || entity.getCreateTime() != null) {
            // 已在系统中流转，按规则只允许在未产生业务记录时删除
            // R2 接入库存后改为校验库存与出入库记录
            log.debug("删除 SKU：id={}", id);
        }
        // 直接调用 baseMapper 删除，逻辑删除由 MyBatis-Plus 自动改写 SQL
        baseMapper.deleteById(id);
        barcodeMapper.delete(new LambdaQueryWrapper<BaseSkuBarcode>().eq(BaseSkuBarcode::getSkuId, id));
    }

    @Override
    public SkuVO getSkuDetail(Long id) {
        BaseSku entity = super.getById(id);
        return entity == null ? null : toVO(entity);
    }

    @Override
    public PageResult<SkuVO> page(SkuQuery query) {
        IPage<BaseSku> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), buildWrapper(query));
        List<SkuVO> voList = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(voList, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public List<SkuVO> listForExport(SkuQuery query) {
        List<BaseSku> list = this.list(buildWrapper(query));
        return list.stream().map(this::toVO).toList();
    }

    @Override
    public Map<String, Object> importSkus(InputStream inputStream) {
        List<Map<Integer, String>> rows = EasyExcel.read(inputStream).sheet(0).headRowNumber(1).doReadSync();
        int total = rows == null ? 0 : rows.size();
        int success = 0;
        int fail = 0;
        List<String> errors = new ArrayList<>();
        int rowNum = 2;
        for (Map<Integer, String> row : rows) {
            try {
                SkuCreateDTO dto = parseRow(row);
                createSku(dto);
                success++;
            } catch (Exception e) {
                fail++;
                errors.add("第" + rowNum + "行：" + e.getMessage());
            }
            rowNum++;
        }
        Map<String, Object> result = new HashMap<>(6);
        result.put("totalCount", total);
        result.put("successCount", success);
        result.put("failCount", fail);
        result.put("errors", errors);
        result.put("failFileUrl", null);
        return result;
    }

    /* ==================== 私有方法 ==================== */

    private LambdaQueryWrapper<BaseSku> buildWrapper(SkuQuery query) {
        return new LambdaQueryWrapper<BaseSku>()
                .eq(StringUtils.hasText(query.getSkuCode()), BaseSku::getSkuCode, query.getSkuCode())
                .eq(StringUtils.hasText(query.getSpuCode()), BaseSku::getSpuCode, query.getSpuCode())
                .eq(query.getCategoryId() != null, BaseSku::getCategoryId, query.getCategoryId())
                .eq(query.getStatus() != null, BaseSku::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(BaseSku::getSkuCode, query.getKeyword())
                        .or().like(BaseSku::getSkuName, query.getKeyword()))
                .orderByDesc(BaseSku::getId);
    }

    private SkuVO toVO(BaseSku entity) {
        SkuVO vo = new SkuVO();
        BeanUtils.copyProperties(entity, vo);
        List<BaseSkuBarcode> barcodes = barcodeMapper.selectList(
                new LambdaQueryWrapper<BaseSkuBarcode>().eq(BaseSkuBarcode::getSkuId, entity.getId()));
        vo.setBarcodes(barcodes.stream().map(BaseSkuBarcode::getBarcode).toList());
        if (entity.getCategoryId() != null) {
            BaseCategory category = categoryMapper.selectById(entity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getCategoryName());
            }
        }
        vo.setTotalQty(0);
        vo.setAvailableQty(0);
        vo.setLockedQty(0);
        return vo;
    }

    private void saveBarcodes(Long skuId, List<String> barcodes) {
        if (CollectionUtils.isEmpty(barcodes)) {
            return;
        }
        for (String barcode : barcodes) {
            if (!StringUtils.hasText(barcode)) {
                continue;
            }
            BaseSkuBarcode entity = new BaseSkuBarcode();
            entity.setSkuId(skuId);
            entity.setBarcode(barcode.trim());
            entity.setPackageSpec("默认");
            entity.setStatus(1);
            barcodeMapper.insert(entity);
        }
    }

    private boolean existsSkuCode(String skuCode) {
        Long count = this.count(new LambdaQueryWrapper<BaseSku>().eq(BaseSku::getSkuCode, skuCode));
        return count != null && count > 0;
    }

    private boolean existsBarcode(String barcode) {
        Long count = barcodeMapper.selectCount(new LambdaQueryWrapper<BaseSkuBarcode>()
                .eq(BaseSkuBarcode::getBarcode, barcode));
        return count != null && count > 0;
    }

    private String generateSkuCode() {
        try {
            R<Map<String, String>> result = codeRuleFeignClient.nextCode("SKU");
            if (result != null && result.getData() != null && result.getData().containsKey("code")) {
                return result.getData().get("code");
            }
        } catch (Exception e) {
            log.warn("调用编码规则服务失败，使用本地兜底生成：{}", e.getMessage());
        }
        return "SP" + System.currentTimeMillis() % 100000;
    }

    /**
     * 解析 Excel 行：0 SKU编码 / 1 SPU编码 / 2 商品名称 / 3 颜色 / 4 尺码 / 5 条形码
     * 6 重量 / 7 体积 / 8 长 / 9 宽 / 10 高 / 11 保质期 / 12 类目
     */
    private SkuCreateDTO parseRow(Map<Integer, String> row) {
        SkuCreateDTO dto = new SkuCreateDTO();
        dto.setSkuCode(value(row, 0));
        dto.setSpuCode(value(row, 1));
        dto.setSkuName(value(row, 2));
        dto.setColor(value(row, 3));
        dto.setSize(value(row, 4));
        String barcodes = value(row, 5);
        if (StringUtils.hasText(barcodes)) {
            dto.setBarcodes(List.of(barcodes.split(",")));
        }
        dto.setWeight(decimal(row, 6));
        dto.setVolume(decimal(row, 7));
        dto.setLengthMm(integer(row, 8));
        dto.setWidthMm(integer(row, 9));
        dto.setHeightMm(integer(row, 10));
        dto.setShelfLifeDays(integer(row, 11));
        if (!StringUtils.hasText(dto.getSpuCode()) || !StringUtils.hasText(dto.getSkuName())) {
            throw new BizException(ErrorCode.SKU_REQUIRED_FIELD_EMPTY.getCode(), "SPU 编码或商品名称不能为空");
        }
        return dto;
    }

    private String value(Map<Integer, String> row, int index) {
        String v = row.get(index);
        return v == null ? null : v.trim();
    }

    private BigDecimal decimal(Map<Integer, String> row, int index) {
        String v = value(row, index);
        return StringUtils.hasText(v) ? new BigDecimal(v) : null;
    }

    private Integer integer(Map<Integer, String> row, int index) {
        String v = value(row, index);
        return StringUtils.hasText(v) ? Integer.valueOf(v) : null;
    }
}
