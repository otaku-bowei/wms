package com.wms.base.controller;

import com.wms.base.domain.dto.LocationBatchDTO;
import com.wms.base.domain.dto.LocationCreateDTO;
import com.wms.base.domain.entity.BaseLocation;
import com.wms.base.domain.query.LocationQuery;
import com.wms.base.domain.vo.LocationLayoutVO;
import com.wms.base.service.LocationService;
import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 库位管理接口
 *
 * @author WMS
 */
@Tag(name = "库位管理", description = "库位增删改查、批量创建、2D 平面图")
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Operation(summary = "分页查询库位")
    @GetMapping
    @RequirePermission("location:view")
    public R<PageResult<BaseLocation>> page(LocationQuery query) {
        return R.ok(locationService.page(query));
    }

    @Operation(summary = "新增库位")
    @PostMapping
    @RequirePermission("location:create")
    @OperationLog(module = "LOCATION", type = OperationType.CREATE, description = "新增库位")
    public R<Long> create(@Valid @RequestBody LocationCreateDTO dto) {
        return R.ok(locationService.createLocation(dto));
    }

    @Operation(summary = "批量创建库位")
    @PostMapping("/batch")
    @RequirePermission("location:batch")
    @OperationLog(module = "LOCATION", type = OperationType.CREATE, description = "批量创建库位")
    public R<Map<String, Object>> batchCreate(@Valid @RequestBody LocationBatchDTO dto) {
        int created = locationService.batchCreate(dto);
        Map<String, Object> data = new HashMap<>(2);
        data.put("successCount", created);
        return R.ok(data);
    }

    @Operation(summary = "查询库位详情")
    @GetMapping("/{id}")
    @RequirePermission("location:view")
    public R<BaseLocation> detail(@PathVariable("id") Long id) {
        return R.ok(locationService.getById(id));
    }

    @Operation(summary = "修改库位")
    @PutMapping("/{id}")
    @RequirePermission("location:edit")
    @OperationLog(module = "LOCATION", type = OperationType.UPDATE, description = "修改库位")
    public R<Void> update(@PathVariable("id") Long id,
                          @RequestParam(value = "locationType", required = false) String locationType,
                          @RequestParam(value = "maxWeight", required = false) BigDecimal maxWeight,
                          @RequestParam(value = "maxVolume", required = false) BigDecimal maxVolume) {
        locationService.updateLocation(id, locationType, maxWeight, maxVolume);
        return R.<Void>ok();
    }

    @Operation(summary = "变更库位状态")
    @PutMapping("/{id}/status")
    @RequirePermission("location:edit")
    @OperationLog(module = "LOCATION", type = OperationType.LOCK, description = "变更库位状态")
    public R<Void> changeStatus(@PathVariable("id") Long id,
                                @RequestParam("status") String status,
                                @RequestParam(value = "reason", required = false) String reason) {
        locationService.changeStatus(id, status, reason);
        return R.<Void>ok();
    }

    @Operation(summary = "查询库位 2D 平面图")
    @GetMapping("/layout")
    @RequirePermission("location:view")
    public R<LocationLayoutVO> layout(@RequestParam("warehouseId") Long warehouseId,
                                      @RequestParam("zoneId") Long zoneId,
                                      @RequestParam(value = "shelf", required = false) Integer shelf) {
        return R.ok(locationService.layout(warehouseId, zoneId, shelf));
    }
}
