package com.wms.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.domain.dto.WarehouseDTO;
import com.wms.base.domain.dto.ZoneDTO;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.domain.query.WarehouseQuery;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.BaseZoneMapper;
import com.wms.base.service.WarehouseService;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 仓库与区域服务实现
 *
 * @author WMS
 */
@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl extends ServiceImpl<BaseWarehouseMapper, BaseWarehouse> implements WarehouseService {

    private final BaseZoneMapper zoneMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWarehouse(WarehouseDTO dto) {
        if (!StringUtils.hasText(dto.getWarehouseCode())) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "仓库编码不能为空");
        }
        if (existsCode(dto.getWarehouseCode(), null)) {
            throw new BizException(ErrorCode.WAREHOUSE_CODE_EXISTS);
        }
        BaseWarehouse entity = new BaseWarehouse();
        BeanUtils.copyProperties(dto, entity);
        this.save(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarehouse(Long id, WarehouseDTO dto) {
        BaseWarehouse entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.WAREHOUSE_NOT_FOUND);
        }
        // 编码不可修改
        entity.setWarehouseName(dto.getWarehouseName());
        entity.setWarehouseType(dto.getWarehouseType());
        entity.setRegion(dto.getRegion());
        entity.setAddress(dto.getAddress());
        entity.setContactName(dto.getContactName());
        entity.setContactPhone(dto.getContactPhone());
        entity.setRemark(dto.getRemark());
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status) {
        BaseWarehouse entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.WAREHOUSE_NOT_FOUND);
        }
        entity.setStatus(status);
        this.updateById(entity);
    }

    @Override
    public BaseWarehouse getById(Long id) {
        // 调用 ServiceImpl 实现，避免使用 this 导致递归
        return super.getById(id);
    }

    @Override
    public PageResult<BaseWarehouse> page(WarehouseQuery query) {
        LambdaQueryWrapper<BaseWarehouse> wrapper = new LambdaQueryWrapper<BaseWarehouse>()
                .like(StringUtils.hasText(query.getKeyword()), BaseWarehouse::getWarehouseCode, query.getKeyword())
                .or().like(StringUtils.hasText(query.getKeyword()), BaseWarehouse::getWarehouseName, query.getKeyword())
                .eq(StringUtils.hasText(query.getWarehouseType()), BaseWarehouse::getWarehouseType, query.getWarehouseType())
                .eq(query.getStatus() != null, BaseWarehouse::getStatus, query.getStatus())
                .orderByAsc(BaseWarehouse::getId);
        IPage<BaseWarehouse> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageConvert.toResult(page);
    }

    @Override
    public List<BaseWarehouse> listEnabled() {
        return this.list(new LambdaQueryWrapper<BaseWarehouse>()
                .eq(BaseWarehouse::getStatus, 1)
                .orderByAsc(BaseWarehouse::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createZone(Long warehouseId, ZoneDTO dto) {
        BaseWarehouse warehouse = this.getById(warehouseId);
        if (warehouse == null) {
            throw new BizException(ErrorCode.WAREHOUSE_NOT_FOUND);
        }
        Long count = zoneMapper.selectCount(new LambdaQueryWrapper<BaseZone>()
                .eq(BaseZone::getWarehouseId, warehouseId)
                .eq(BaseZone::getZoneCode, dto.getZoneCode()));
        if (count != null && count > 0) {
            throw new BizException(ErrorCode.ZONE_CODE_EXISTS);
        }
        BaseZone zone = new BaseZone();
        BeanUtils.copyProperties(dto, zone);
        zone.setWarehouseId(warehouseId);
        zoneMapper.insert(zone);
        return zone.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateZone(Long zoneId, ZoneDTO dto) {
        BaseZone zone = zoneMapper.selectById(zoneId);
        if (zone == null) {
            throw new BizException(ErrorCode.ZONE_NOT_FOUND);
        }
        zone.setZoneName(dto.getZoneName());
        zone.setZoneType(dto.getZoneType());
        zone.setTurnoverZone(dto.getTurnoverZone());
        zone.setStatus(dto.getStatus());
        zoneMapper.updateById(zone);
    }

    @Override
    public List<BaseZone> listZones(Long warehouseId) {
        return zoneMapper.selectList(new LambdaQueryWrapper<BaseZone>()
                .eq(BaseZone::getWarehouseId, warehouseId)
                .orderByAsc(BaseZone::getZoneCode));
    }

    private boolean existsCode(String warehouseCode, Long excludeId) {
        Long count = this.count(new LambdaQueryWrapper<BaseWarehouse>()
                .eq(BaseWarehouse::getWarehouseCode, warehouseCode)
                .ne(excludeId != null, BaseWarehouse::getId, excludeId));
        return count != null && count > 0;
    }
}
