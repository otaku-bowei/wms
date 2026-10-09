package com.wms.base.controller;

import com.wms.base.domain.dto.WarehouseDTO;
import com.wms.base.domain.dto.ZoneDTO;
import com.wms.base.domain.entity.BaseWarehouse;
import com.wms.base.domain.entity.BaseZone;
import com.wms.base.domain.query.WarehouseQuery;
import com.wms.base.service.WarehouseService;
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

import java.util.List;

/**
 * 仓库管理接口
 *
 * @author WMS
 */
@Tag(name = "仓库管理", description = "仓库增删改查、区域管理")
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Operation(summary = "分页查询仓库")
    @GetMapping
    @RequirePermission("warehouse:view")
    public R<PageResult<BaseWarehouse>> page(WarehouseQuery query) {
        return R.ok(warehouseService.page(query));
    }

    @Operation(summary = "新增仓库")
    @PostMapping
    @RequirePermission("warehouse:create")
    @OperationLog(module = "WAREHOUSE", type = OperationType.CREATE, description = "新增仓库")
    public R<Long> create(@Valid @RequestBody WarehouseDTO dto) {
        return R.ok(warehouseService.createWarehouse(dto));
    }

    @Operation(summary = "查询仓库详情")
    @GetMapping("/{id}")
    @RequirePermission("warehouse:view")
    public R<BaseWarehouse> detail(@PathVariable("id") Long id) {
        return R.ok(warehouseService.getById(id));
    }

    @Operation(summary = "修改仓库")
    @PutMapping("/{id}")
    @RequirePermission("warehouse:edit")
    @OperationLog(module = "WAREHOUSE", type = OperationType.UPDATE, description = "修改仓库")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody WarehouseDTO dto) {
        warehouseService.updateWarehouse(id, dto);
        return R.<Void>ok();
    }

    @Operation(summary = "启用/停用仓库")
    @PutMapping("/{id}/status")
    @RequirePermission("warehouse:edit")
    @OperationLog(module = "WAREHOUSE", type = OperationType.UPDATE, description = "启用停用仓库")
    public R<Void> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        warehouseService.changeStatus(id, status);
        return R.<Void>ok();
    }

    @Operation(summary = "查询启用仓库下拉选项")
    @GetMapping("/options")
    @RequirePermission("warehouse:view")
    public R<List<BaseWarehouse>> options() {
        return R.ok(warehouseService.listEnabled());
    }

    @Operation(summary = "查询仓库区域列表")
    @GetMapping("/{id}/zones")
    @RequirePermission("warehouse:view")
    public R<List<BaseZone>> zones(@PathVariable("id") Long id) {
        return R.ok(warehouseService.listZones(id));
    }

    @Operation(summary = "新增仓库区域")
    @PostMapping("/{id}/zones")
    @RequirePermission("warehouse:edit")
    @OperationLog(module = "WAREHOUSE", type = OperationType.CREATE, description = "新增仓库区域")
    public R<Long> createZone(@PathVariable("id") Long id, @Valid @RequestBody ZoneDTO dto) {
        return R.ok(warehouseService.createZone(id, dto));
    }
}
