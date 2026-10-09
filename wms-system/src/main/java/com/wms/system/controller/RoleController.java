package com.wms.system.controller;

import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色权限管理接口
 *
 * @author WMS
 */
@Tag(name = "角色权限", description = "角色查询、权限配置")
@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "查询角色列表")
    @GetMapping
    @RequirePermission("role:view")
    public R<List<SysRole>> list() {
        return R.ok(roleService.listRoles());
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{id}")
    @RequirePermission("role:view")
    public R<SysRole> detail(@PathVariable("id") Long id) {
        return R.ok(roleService.getById(id));
    }

    @Operation(summary = "修改角色描述")
    @PutMapping("/{id}")
    @RequirePermission("role:edit")
    @OperationLog(module = "ROLE", type = OperationType.UPDATE, description = "修改角色")
    public R<Void> update(@PathVariable("id") Long id,
                          @RequestParam(value = "roleName", required = false) String roleName,
                          @RequestParam(value = "description", required = false) String description) {
        roleService.updateRole(id, roleName, description);
        return R.<Void>ok();
    }

    @Operation(summary = "查询角色权限")
    @GetMapping("/{id}/permissions")
    @RequirePermission("role:view")
    public R<Map<String, Set<String>>> permissions(@PathVariable("id") Long id) {
        return R.ok(Map.of("permissions", roleService.getRolePermissionCodes(id)));
    }

    @Operation(summary = "配置角色权限")
    @PutMapping("/{id}/permissions")
    @RequirePermission("role:edit")
    @OperationLog(module = "ROLE", type = OperationType.UPDATE, description = "配置角色权限")
    public R<Void> updatePermissions(@PathVariable("id") Long id,
                                     @RequestBody(required = false) List<String> permissions) {
        roleService.updateRolePermissions(id, permissions);
        return R.<Void>ok();
    }
}
