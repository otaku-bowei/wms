package com.wms.auth.service.impl;

import com.wms.api.system.client.LoginLogFeignClient;
import com.wms.api.system.client.PermissionFeignClient;
import com.wms.api.system.client.UserFeignClient;
import com.wms.api.system.dto.ChangePasswordDTO;
import com.wms.api.system.dto.LoginInfoDTO;
import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.api.system.dto.UserAuthDTO;
import com.wms.auth.domain.dto.ChangePasswordRequest;
import com.wms.auth.domain.dto.LoginDTO;
import com.wms.auth.domain.vo.LoginVO;
import com.wms.auth.domain.vo.TokenVO;
import com.wms.auth.domain.vo.UserInfoVO;
import com.wms.auth.guard.LoginGuard;
import com.wms.auth.service.AuthService;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.ErrorCode;
import com.wms.common.core.result.R;
import com.wms.common.redis.constant.RedisKeys;
import com.wms.common.security.context.LoginUser;
import com.wms.common.security.context.UserContext;
import com.wms.common.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 认证服务实现
 *
 * @author WMS
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String LOGIN_SUCCESS = "SUCCESS";
    private static final String LOGIN_FAIL = "FAIL";
    private static final String LOGIN_LOCKED = "LOCKED";

    private final UserFeignClient userFeignClient;
    private final PermissionFeignClient permissionFeignClient;
    private final LoginLogFeignClient loginLogFeignClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final LoginGuard loginGuard;
    private final StringRedisTemplate redisTemplate;

    @Value("${wms.password.min-length:8}")
    private int passwordMinLength;

    @Override
    public LoginVO login(LoginDTO dto, String ipAddress) {
        String username = dto.getUsername();

        // 1. 账户锁定校验
        if (loginGuard.isLocked(username)) {
            recordLoginLog(username, ipAddress, LOGIN_LOCKED, "账户锁定");
            throw new BizException(ErrorCode.USER_LOCKED);
        }

        // 2. 查询用户
        UserAuthDTO user = queryUser(username);
        if (user == null) {
            recordLoginLog(username, ipAddress, LOGIN_FAIL, "用户名不存在");
            throw new BizException(ErrorCode.USER_CREDENTIAL_ERROR);
        }

        // 3. 账户停用校验
        if (user.getStatus() != null && user.getStatus() == 0) {
            recordLoginLog(username, ipAddress, LOGIN_FAIL, "账户已停用");
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        // 4. 密码校验
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            int remain = loginGuard.recordFail(username);
            recordLoginLog(username, ipAddress, LOGIN_FAIL, "密码错误");
            throw new BizException(ErrorCode.USER_CREDENTIAL_ERROR.getCode(), "密码错误，剩余 " + remain + " 次尝试机会");
        }
        loginGuard.clearFail(username);

        // 5. 生成 Token
        LoginUser loginUser = buildLoginUser(user);
        String token = jwtTokenProvider.generateToken(loginUser);

        // 6. 更新最后登录信息
        updateLoginInfo(user.getUserId(), ipAddress);

        // 7. 记录登录日志
        recordLoginLog(username, ipAddress, LOGIN_SUCCESS, null);

        return LoginVO.builder()
                .token(token)
                .expiresIn(jwtTokenProvider.getExpireSeconds())
                .userId(user.getUserId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(user.getRoleCode())
                .warehouseId(user.getWarehouseId())
                .forceChangePassword(user.getForceChangePassword() != null && user.getForceChangePassword() == 1)
                .build();
    }

    @Override
    public void logout(String token) {
        if (!StringUtils.hasText(token)) {
            return;
        }
        try {
            String jti = jwtTokenProvider.getJti(token);
            long ttlSeconds = Math.max(1, (jwtTokenProvider.getExpiration(token) - System.currentTimeMillis()) / 1000);
            redisTemplate.opsForValue().set(RedisKeys.tokenBlacklist(jti), "1", Duration.ofSeconds(ttlSeconds));
            UserContext.clear();
        } catch (Exception e) {
            log.warn("退出登录处理失败：{}", e.getMessage());
        }
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BizException(ErrorCode.PARAM_ERROR.getCode(), "两次输入的新密码不一致");
        }
        validatePassword(request.getNewPassword());
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setOldPassword(request.getOldPassword());
        dto.setNewPassword(request.getNewPassword());
        R<Void> result = userFeignClient.changePassword(userId, dto);
        if (result == null || !result.isSuccess()) {
            throw new BizException(ErrorCode.OLD_PASSWORD_ERROR);
        }
    }

    @Override
    public UserInfoVO currentUser(Long userId) {
        LoginUser loginUser = UserContext.get();
        if (loginUser == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return UserInfoVO.builder()
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .realName(loginUser.getRealName())
                .roleCode(loginUser.getRoleCode())
                .warehouseId(loginUser.getWarehouseId())
                .build();
    }

    @Override
    public TokenVO menus(Long userId) {
        Set<String> permissions = loadPermissions(userId);
        R<List<PermissionNodeDTO>> menuResult = permissionFeignClient.getUserMenus(userId);
        List<PermissionNodeDTO> menus = menuResult == null ? List.of() : menuResult.getData();
        return TokenVO.builder()
                .menus(menus)
                .permissions(permissions)
                .build();
    }

    @Override
    public TokenVO refresh(String token) {
        Claims claims = jwtTokenProvider.parseToken(token);
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(Long.valueOf(claims.getSubject()));
        loginUser.setUsername(claims.get(JwtTokenProvider.CLAIM_USERNAME, String.class));
        loginUser.setRealName(claims.get(JwtTokenProvider.CLAIM_REAL_NAME, String.class));
        loginUser.setRoleCode(claims.get(JwtTokenProvider.CLAIM_ROLE_CODE, String.class));
        Object warehouseId = claims.get(JwtTokenProvider.CLAIM_WAREHOUSE_ID);
        loginUser.setWarehouseId(warehouseId == null ? null : Long.valueOf(String.valueOf(warehouseId)));
        loginUser.setPermissions(loadPermissions(loginUser.getUserId()));

        String newToken = jwtTokenProvider.generateToken(loginUser);
        // 旧 Token 作废
        logout(token);
        return TokenVO.builder()
                .token(newToken)
                .expiresIn(jwtTokenProvider.getExpireSeconds())
                .build();
    }

    /* ==================== 私有方法 ==================== */

    private UserAuthDTO queryUser(String username) {
        try {
            R<UserAuthDTO> result = userFeignClient.getByUsername(username);
            return result == null ? null : result.getData();
        } catch (Exception e) {
            log.error("查询用户失败：username={}", username, e);
            throw new BizException(ErrorCode.SYSTEM_ERROR.getCode(), "用户服务不可用");
        }
    }

    private LoginUser buildLoginUser(UserAuthDTO user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setUsername(user.getUsername());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoleCode(user.getRoleCode());
        loginUser.setWarehouseId(user.getWarehouseId());
        loginUser.setPermissions(loadPermissions(user.getUserId()));
        return loginUser;
    }

    private Set<String> loadPermissions(Long userId) {
        String key = RedisKeys.userPerms(userId);
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(cached)) {
                return Set.of(cached.split(","));
            }
        } catch (Exception e) {
            log.warn("读取权限缓存失败：userId={}", userId);
        }
        Set<String> permissions = Set.of();
        try {
            R<Set<String>> result = permissionFeignClient.getUserPermissionCodes(userId);
            if (result != null && result.getData() != null) {
                permissions = result.getData();
            }
        } catch (Exception e) {
            log.warn("查询用户权限失败：userId={}", userId);
        }
        if (!permissions.isEmpty()) {
            try {
                redisTemplate.opsForValue().set(key, String.join(",", permissions), Duration.ofMinutes(30));
            } catch (Exception e) {
                log.warn("写入权限缓存失败：userId={}", userId);
            }
        }
        return permissions;
    }

    private void updateLoginInfo(Long userId, String ipAddress) {
        try {
            LoginInfoDTO dto = new LoginInfoDTO();
            dto.setIpAddress(ipAddress);
            userFeignClient.updateLoginInfo(userId, dto);
        } catch (Exception e) {
            log.warn("更新登录信息失败：userId={}", userId);
        }
    }

    private void recordLoginLog(String username, String ipAddress, String result, String failReason) {
        try {
            com.wms.api.system.dto.LoginLogDTO dto = new com.wms.api.system.dto.LoginLogDTO();
            dto.setUsername(username);
            dto.setIpAddress(ipAddress);
            dto.setLoginResult(result);
            dto.setFailReason(failReason);
            loginLogFeignClient.record(dto);
        } catch (Exception e) {
            log.warn("记录登录日志失败：username={}", username);
        }
    }

    private void validatePassword(String password) {
        if (password.length() < passwordMinLength) {
            throw new BizException(ErrorCode.PASSWORD_TOO_SHORT.getCode(),
                    "密码长度不能少于 " + passwordMinLength + " 位");
        }
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BizException(ErrorCode.PASSWORD_NEED_LETTER_NUMBER);
        }
    }
}
