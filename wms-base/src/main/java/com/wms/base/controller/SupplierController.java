package com.wms.base.controller;

import com.wms.base.domain.dto.SupplierDTO;
import com.wms.base.domain.entity.BaseSupplier;
import com.wms.base.domain.query.SupplierQuery;
import com.wms.base.service.SupplierService;
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

/**
 * 供应商管理接口
 *
 * @author WMS
 */
@Tag(name = "供应商管理", description = "供应商增删改查")
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @Operation(summary = "分页查询供应商")
    @GetMapping
    @RequirePermission("supplier:view")
    public R<PageResult<BaseSupplier>> page(SupplierQuery query) {
        return R.ok(supplierService.page(query));
    }

    @Operation(summary = "新增供应商")
    @PostMapping
    @RequirePermission("supplier:create")
    @OperationLog(module = "SUPPLIER", type = OperationType.CREATE, description = "新增供应商")
    public R<Long> create(@Valid @RequestBody SupplierDTO dto) {
        return R.ok(supplierService.createSupplier(dto));
    }

    @Operation(summary = "查询供应商详情")
    @GetMapping("/{id}")
    @RequirePermission("supplier:view")
    public R<BaseSupplier> detail(@PathVariable("id") Long id) {
        return R.ok(supplierService.getById(id));
    }

    @Operation(summary = "修改供应商")
    @PutMapping("/{id}")
    @RequirePermission("supplier:edit")
    @OperationLog(module = "SUPPLIER", type = OperationType.UPDATE, description = "修改供应商")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody SupplierDTO dto) {
        supplierService.updateSupplier(id, dto);
        return R.<Void>ok();
    }

    @Operation(summary = "启用/停用供应商")
    @PutMapping("/{id}/status")
    @RequirePermission("supplier:edit")
    @OperationLog(module = "SUPPLIER", type = OperationType.UPDATE, description = "启用停用供应商")
    public R<Void> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        supplierService.changeStatus(id, status);
        return R.<Void>ok();
    }
}
