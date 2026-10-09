package com.wms.system.controller;

import com.wms.api.system.dto.ChangePasswordDTO;
import com.wms.api.system.dto.LoginInfoDTO;
import com.wms.api.system.dto.LoginLogDTO;
import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.api.system.dto.UserAuthDTO;
import com.wms.common.core.result.R;
import com.wms.system.domain.entity.SysRole;
import com.wms.system.domain.entity.SysUser;
import com.wms.system.service.CodeRuleService;
import com.wms.system.service.LogService;
import com.wms.system.service.RoleService;
import com.wms.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 内部服务接口（供 auth 服务通过 Feign 调用）
 *
 * <p>路径 /internal/** 不对外暴露，由网关拦截。
 *
 * @author WMS
 */
@Tag(name = "内部接口", description = "供认证服务等内部调用")
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserService userService;
    private final RoleService roleService;
    private final LogService logService;
    private final CodeRuleService codeRuleService;

    @Operation(summary = "根据用户名查询用户")
    @GetMapping("/users/by-username/{username}")
    public R<UserAuthDTO> getByUsername(@PathVariable("username") String username) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            return R.ok(null);
        }
        UserAuthDTO dto = new UserAuthDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPassword(user.getPassword());
        dto.setRoleId(user.getRoleId());
        dto.setWarehouseId(user.getWarehouseId());
        dto.setStatus(user.getStatus());
        dto.setForceChangePassword(user.getForceChangePassword());
        SysRole role = user.getRoleId() == null ? null : roleService.getById(user.getRoleId());
        if (role != null) {
            dto.setRoleCode(role.getRoleCode());
        }
        return R.ok(dto);
    }

    @Operation(summary = "更新最后登录信息")
    @PutMapping("/users/{userId}/login-info")
    public R<Void> updateLoginInfo(@PathVariable("userId") Long userId, @RequestBody LoginInfoDTO dto) {
        userService.updateLoginInfo(userId, dto == null ? null : dto.getIpAddress());
        return R.<Void>ok();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/users/{userId}/password")
    public R<Void> changePassword(@PathVariable("userId") Long userId, @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return R.<Void>ok();
    }

    @Operation(summary = "查询用户权限标识")
    @GetMapping("/permissions/user/{userId}")
    public R<Set<String>> userPermissionCodes(@PathVariable("userId") Long userId) {
        return R.ok(roleService.getUserPermissionCodes(userId));
    }

    @Operation(summary = "查询用户菜单树")
    @GetMapping("/permissions/menu/{userId}")
    public R<List<PermissionNodeDTO>> userMenus(@PathVariable("userId") Long userId) {
        return R.ok(roleService.getUserMenus(userId));
    }

    @Operation(summary = "记录登录日志")
    @PostMapping("/logs/login")
    public R<Void> recordLoginLog(@RequestBody LoginLogDTO dto) {
        logService.saveLoginLog(dto);
        return R.<Void>ok();
    }

    @Operation(summary = "生成下一个编码")
    @GetMapping("/config/next-code/{ruleType}")
    public R<Map<String, String>> nextCode(@PathVariable("ruleType") String ruleType) {
        String code = codeRuleService.generateNextCode(ruleType);
        Map<String, String> data = new java.util.HashMap<>(2);
        data.put("code", code);
        return R.ok(data);
    }
}
