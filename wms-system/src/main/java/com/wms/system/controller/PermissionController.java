package com.wms.system.controller;

import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.common.core.result.R;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限接口
 *
 * @author WMS
 */
@Tag(name = "权限管理", description = "权限树查询")
@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final RoleService roleService;

    @Operation(summary = "查询权限树")
    @GetMapping("/tree")
    @RequirePermission("role:view")
    public R<List<PermissionNodeDTO>> tree() {
        return R.ok(roleService.getPermissionTree());
    }
}
