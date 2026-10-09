package com.wms.system.controller;

import com.wms.common.core.result.PageResult;
import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.annotation.RequirePermission;
import com.wms.system.domain.dto.UserCreateDTO;
import com.wms.system.domain.dto.UserUpdateDTO;
import com.wms.system.domain.query.UserQuery;
import com.wms.system.domain.vo.UserVO;
import com.wms.system.service.UserService;
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

import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理接口
 *
 * @author WMS
 */
@Tag(name = "用户管理", description = "用户新增、修改、启停、重置密码")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    @RequirePermission("user:view")
    public R<PageResult<UserVO>> page(UserQuery query) {
        return R.ok(userService.page(query));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @RequirePermission("user:create")
    @OperationLog(module = "USER", type = OperationType.CREATE, description = "新增用户")
    public R<Long> create(@Valid @RequestBody UserCreateDTO dto) {
        return R.ok(userService.createUser(dto));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    @RequirePermission("user:view")
    public R<UserVO> detail(@PathVariable("id") Long id) {
        return R.ok(userService.getUserDetail(id));
    }

    @Operation(summary = "修改用户")
    @PutMapping("/{id}")
    @RequirePermission("user:edit")
    @OperationLog(module = "USER", type = OperationType.UPDATE, description = "修改用户")
    public R<Void> update(@PathVariable("id") Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return R.<Void>ok();
    }

    @Operation(summary = "启用/停用用户")
    @PutMapping("/{id}/status")
    @RequirePermission("user:edit")
    @OperationLog(module = "USER", type = OperationType.UPDATE, description = "启用停用用户")
    public R<Void> changeStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        userService.changeStatus(id, status);
        return R.<Void>ok();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    @RequirePermission("user:reset")
    @OperationLog(module = "USER", type = OperationType.RESET, description = "重置用户密码")
    public R<Map<String, String>> resetPassword(@PathVariable("id") Long id) {
        String password = userService.resetPassword(id);
        Map<String, String> data = new HashMap<>(2);
        data.put("newPassword", password);
        return R.ok(data);
    }

    @Operation(summary = "校验用户名是否可用")
    @GetMapping("/check-username")
    @RequirePermission("user:view")
    public R<Map<String, Boolean>> checkUsername(@RequestParam("username") String username) {
        boolean exists = userService.existsUsername(username);
        Map<String, Boolean> data = new HashMap<>(2);
        data.put("available", !exists);
        return R.ok(data);
    }
}
