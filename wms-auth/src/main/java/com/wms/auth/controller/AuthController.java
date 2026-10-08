package com.wms.auth.controller;

import com.wms.auth.domain.dto.ChangePasswordRequest;
import com.wms.auth.domain.dto.LoginDTO;
import com.wms.auth.domain.vo.LoginVO;
import com.wms.auth.domain.vo.TokenVO;
import com.wms.auth.domain.vo.UserInfoVO;
import com.wms.auth.service.AuthService;
import com.wms.auth.util.IpUtils;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
import com.wms.common.log.annotation.OperationLog;
import com.wms.common.log.enums.OperationType;
import com.wms.common.security.context.UserContext;
import com.wms.common.web.interceptor.TokenAuthInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 *
 * @author WMS
 */
@Tag(name = "认证管理", description = "登录、登出、改密、Token 刷新、菜单权限")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    @OperationLog(module = "AUTH", type = OperationType.LOGIN, description = "用户登录")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        return R.ok(authService.login(dto, IpUtils.getIpAddress(request)));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    @OperationLog(module = "AUTH", type = OperationType.LOGOUT, description = "退出登录")
    public R<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        authService.logout(TokenAuthInterceptor.resolveToken(authorization));
        return R.<Void>ok();
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    @OperationLog(module = "AUTH", type = OperationType.UPDATE, description = "修改密码")
    public R<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        authService.changePassword(userId, request);
        return R.<Void>ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/current")
    public R<UserInfoVO> current() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(authService.currentUser(userId));
    }

    @Operation(summary = "获取当前用户菜单与权限")
    @GetMapping("/menus")
    public R<TokenVO> menus() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(authService.menus(userId));
    }

    @Operation(summary = "刷新 Token")
    @PostMapping("/refresh")
    public R<TokenVO> refresh(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        String token = TokenAuthInterceptor.resolveToken(authorization);
        if (token == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return R.ok(authService.refresh(token));
    }
}
