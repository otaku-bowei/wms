package com.wms.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wms.base.domain.dto.LocationBatchDTO;
import com.wms.base.domain.dto.LocationCreateDTO;
import com.wms.base.domain.entity.BaseLocation;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.domain.query.LocationQuery;
import com.wms.base.domain.vo.LocationCellVO;
import com.wms.base.domain.vo.LocationLayoutVO;
import com.wms.base.mapper.BaseLocationMapper;
import com.wms.base.mapper.BaseWarehouseMapper;
import com.wms.base.mapper.BaseZoneMapper;
import com.wms.base.service.LocationService;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.PageResult;
import com.wms.common.mybatis.convert.PageConvert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 库位服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationServiceImpl extends ServiceImpl<BaseLocationMapper, BaseLocation> implements LocationService {

    private static final String STATUS_FREE = "FREE";
    private static final String STATUS_OCCUPIED = "OCCUPIED";
    private static final String STATUS_LOCKED = "LOCKED";
    private static final String STATUS_MAINTENANCE = "MAINTENANCE";
    private static final BigDecimal FULL_THRESHOLD = new BigDecimal("95");

    private final BaseWarehouseMapper warehouseMapper;
    private final BaseZoneMapper zoneMapper;

    /** 单次批量创建上限 */
    @Value("${wms.base.location-batch-limit:5000}")
    private int batchLimit;

    /** 分批插入批次大小 */
    @Value("${wms.base.batch-size:500}")
    private int batchSize;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createLocation(LocationCreateDTO dto) {
        String locationCode = buildLocationCode(dto.getWarehouseId(), dto.getZoneId(),
                dto.getShelfNo(), dto.getLayerNo(), dto.getColumnNo(), dto.getPositionNo());
        if (existsCode(locationCode)) {
            throw new BizException(ErrorCode.LOCATION_CODE_EXISTS);
        }
        BaseLocation entity = new BaseLocation();
        entity.setLocationCode(locationCode);
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setZoneId(dto.getZoneId());
        entity.setShelfNo(dto.getShelfNo());
        entity.setLayerNo(dto.getLayerNo());
        entity.setColumnNo(dto.getColumnNo());
        entity.setPositionNo(dto.getPositionNo());
        entity.setLocationType(dto.getLocationType());
        entity.setStatus(STATUS_FREE);
        entity.setMaxWeight(dto.getMaxWeight());
        entity.setMaxVolume(dto.getMaxVolume());
        entity.setUsedWeight(BigDecimal.ZERO);
        entity.setUsedVolume(BigDecimal.ZERO);
        this.save(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(LocationBatchDTO dto) {
        int total = dto.getShelfCount() * dto.getLayerCount() * dto.getColumnCount() * dto.getPositionCount();
        if (total <= 0) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "生成规格数量必须大于 0");
        }
        if (total > batchLimit) {
            throw new BizException(ErrorCode.LOCATION_BATCH_LIMIT);
        }
        String warehouseCode = warehouseCodeOf(dto.getWarehouseId());
        String zoneCode = zoneCodeOf(dto.getZoneId());

        Set<String> existingCodes = new HashSet<>(listExistingCodes(dto.getWarehouseId(), dto.getZoneId()));
        List<BaseLocation> pending = new ArrayList<>(total);
        for (int s = 1; s <= dto.getShelfCount(); s++) {
            for (int l = 1; l <= dto.getLayerCount(); l++) {
                for (int c = 1; c <= dto.getColumnCount(); c++) {
                    for (int p = 1; p <= dto.getPositionCount(); p++) {
                        String code = String.format("%s-%s-%02d-%02d-%02d-%02d",
                                warehouseCode, zoneCode, s, l, c, p);
                        if (existingCodes.contains(code)) {
                            continue;
                        }
                        pending.add(buildEntity(dto, code, s, l, c, p));
                    }
                }
            }
        }
        int created = 0;
        for (int i = 0; i < pending.size(); i += batchSize) {
            List<BaseLocation> sub = pending.subList(i, Math.min(i + batchSize, pending.size()));
            this.saveBatch(sub);
            created += sub.size();
        }
        return created;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLocation(Long id, String type, BigDecimal maxWeight, BigDecimal maxVolume) {
        BaseLocation entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.LOCATION_NOT_FOUND);
        }
        if (maxWeight != null && entity.getUsedWeight() != null && maxWeight.compareTo(entity.getUsedWeight()) < 0) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "最大承重不可低于已用承重");
        }
        if (StringUtils.hasText(type)) {
            entity.setLocationType(type);
        }
        if (maxWeight != null) {
            entity.setMaxWeight(maxWeight);
        }
        if (maxVolume != null) {
            entity.setMaxVolume(maxVolume);
        }
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, String status, String reason) {
        BaseLocation entity = this.getById(id);
        if (entity == null) {
            throw new BizException(ErrorCode.LOCATION_NOT_FOUND);
        }
        // 占用库位不可直接变更为空闲，须通过出入库业务流转
        if (STATUS_OCCUPIED.equals(entity.getStatus()) && STATUS_FREE.equals(status)) {
            throw new BizException(ErrorCode.LOCATION_STATUS_INVALID.getCode(), "已占用库位不可直接置为空闲");
        }
        if (STATUS_FREE.equals(status) && !STATUS_FREE.equals(entity.getStatus())
                && !STATUS_LOCKED.equals(entity.getStatus()) && !STATUS_MAINTENANCE.equals(entity.getStatus())) {
            throw new BizException(ErrorCode.LOCATION_STATUS_INVALID);
        }
        entity.setStatus(status);
        entity.setLockReason(reason);
        entity.setStatusUpdateTime(LocalDateTime.now());
        this.updateById(entity);
    }

    @Override
    public BaseLocation getById(Long id) {
        // 调用 ServiceImpl 实现，避免使用 this 导致递归
        return super.getById(id);
    }

    @Override
    public PageResult<BaseLocation> page(LocationQuery query) {
        LambdaQueryWrapper<BaseLocation> wrapper = new LambdaQueryWrapper<BaseLocation>()
                .eq(query.getWarehouseId() != null, BaseLocation::getWarehouseId, query.getWarehouseId())
                .eq(query.getZoneId() != null, BaseLocation::getZoneId, query.getZoneId())
                .like(StringUtils.hasText(query.getLocationCode()), BaseLocation::getLocationCode, query.getLocationCode())
                .eq(StringUtils.hasText(query.getLocationType()), BaseLocation::getLocationType, query.getLocationType())
                .eq(StringUtils.hasText(query.getStatus()), BaseLocation::getStatus, query.getStatus())
                .orderByAsc(BaseLocation::getLocationCode);
        IPage<BaseLocation> page = this.page(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return PageConvert.toResult(page);
    }

    @Override
    public LocationLayoutVO layout(Long warehouseId, Long zoneId, Integer shelf) {
        BaseZone zone = zoneMapper.selectById(zoneId);
        List<BaseLocation> locations = this.list(new LambdaQueryWrapper<BaseLocation>()
                .eq(BaseLocation::getWarehouseId, warehouseId)
                .eq(BaseLocation::getZoneId, zoneId)
                .eq(shelf != null, BaseLocation::getShelfNo, shelf)
                .orderByAsc(BaseLocation::getShelfNo)
                .orderByAsc(BaseLocation::getLayerNo)
                .orderByAsc(BaseLocation::getColumnNo));

        List<LocationCellVO> cells = locations.stream()
                .map(this::toCell)
                .toList();
        long free = locations.stream().filter(l -> STATUS_FREE.equals(l.getStatus())).count();
        long occupied = locations.stream().filter(l -> STATUS_OCCUPIED.equals(l.getStatus())).count();
        long unavailable = locations.stream()
                .filter(l -> STATUS_LOCKED.equals(l.getStatus()) || STATUS_MAINTENANCE.equals(l.getStatus()))
                .count();

        return LocationLayoutVO.builder()
                .zoneName(zone == null ? null : zone.getZoneName())
                .shelfCount(locations.stream().map(BaseLocation::getShelfNo).distinct().count() == 0 ? 0
                        : locations.stream().mapToInt(BaseLocation::getShelfNo).max().orElse(0))
                .layerCount(locations.stream().mapToInt(BaseLocation::getLayerNo).max().orElse(0))
                .columnCount(locations.stream().mapToInt(BaseLocation::getColumnNo).max().orElse(0))
                .cells(cells)
                .freeCount(free)
                .occupiedCount(occupied)
                .unavailableCount(unavailable)
                .build();
    }

    /* ==================== 私有方法 ==================== */

    private LocationCellVO toCell(BaseLocation location) {
        BigDecimal usageRate = calculateUsageRate(location);
        return LocationCellVO.builder()
                .locationId(location.getId())
                .locationCode(location.getLocationCode())
                .shelf(location.getShelfNo())
                .layer(location.getLayerNo())
                .columnNo(location.getColumnNo())
                .status(location.getStatus())
                .usageRate(usageRate)
                .colorLevel(resolveColorLevel(location.getStatus(), usageRate))
                .build();
    }

    private BigDecimal calculateUsageRate(BaseLocation location) {
        if (location.getMaxWeight() == null || location.getMaxWeight().compareTo(BigDecimal.ZERO) <= 0
                || location.getUsedWeight() == null) {
            return BigDecimal.ZERO;
        }
        return location.getUsedWeight()
                .multiply(new BigDecimal("100"))
                .divide(location.getMaxWeight(), 2, RoundingMode.HALF_UP);
    }

    private String resolveColorLevel(String status, BigDecimal usageRate) {
        if (STATUS_LOCKED.equals(status) || STATUS_MAINTENANCE.equals(status)) {
            return "GRAY";
        }
        if (STATUS_FREE.equals(status)) {
            return "GREEN";
        }
        if (usageRate.compareTo(FULL_THRESHOLD) >= 0) {
            return "RED";
        }
        return "YELLOW";
    }

    private String buildLocationCode(Long warehouseId, Long zoneId, int shelf, int layer, int columnNo, int position) {
        return String.format("%s-%s-%02d-%02d-%02d-%02d",
                warehouseCodeOf(warehouseId), zoneCodeOf(zoneId), shelf, layer, columnNo, position);
    }

    private String warehouseCodeOf(Long warehouseId) {
        BaseWarehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new BizException(ErrorCode.WAREHOUSE_NOT_FOUND);
        }
        return warehouse.getWarehouseCode();
    }

    private String zoneCodeOf(Long zoneId) {
        BaseZone zone = zoneMapper.selectById(zoneId);
        if (zone == null) {
            throw new BizException(ErrorCode.ZONE_NOT_FOUND);
        }
        return zone.getZoneCode();
    }

    private boolean existsCode(String locationCode) {
        Long count = this.count(new LambdaQueryWrapper<BaseLocation>().eq(BaseLocation::getLocationCode, locationCode));
        return count != null && count > 0;
    }

    private List<String> listExistingCodes(Long warehouseId, Long zoneId) {
        List<BaseLocation> existing = this.list(new LambdaQueryWrapper<BaseLocation>()
                .eq(BaseLocation::getWarehouseId, warehouseId)
                .eq(BaseLocation::getZoneId, zoneId));
        return existing.stream().map(BaseLocation::getLocationCode).toList();
    }

    private BaseLocation buildEntity(LocationBatchDTO dto, String code, int s, int l, int c, int p) {
        BaseLocation entity = new BaseLocation();
        entity.setLocationCode(code);
        entity.setWarehouseId(dto.getWarehouseId());
        entity.setZoneId(dto.getZoneId());
        entity.setShelfNo(s);
        entity.setLayerNo(l);
        entity.setColumnNo(c);
        entity.setPositionNo(p);
        entity.setLocationType(dto.getLocationType());
        entity.setStatus(STATUS_FREE);
        entity.setMaxWeight(dto.getMaxWeight());
        entity.setMaxVolume(dto.getMaxVolume());
        entity.setUsedWeight(BigDecimal.ZERO);
        entity.setUsedVolume(BigDecimal.ZERO);
        return entity;
    }
}
