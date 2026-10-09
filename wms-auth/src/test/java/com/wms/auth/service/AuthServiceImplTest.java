package com.wms.auth.service;

import com.wms.api.system.client.LoginLogFeignClient;
import com.wms.api.system.client.PermissionFeignClient;
import com.wms.api.system.client.UserFeignClient;
import com.wms.api.system.dto.ChangePasswordDTO;
import com.wms.api.system.dto.PermissionNodeDTO;
import com.wms.api.system.dto.UserAuthDTO;
import com.wms.auth.domain.dto.ChangePasswordRequest;
import com.wms.auth.domain.dto.LoginDTO;
import com.wms.auth.domain.vo.LoginVO;
import com.wms.auth.domain.vo.TokenVO;
import com.wms.auth.guard.LoginGuard;
import com.wms.auth.service.impl.AuthServiceImpl;
import com.wms.common.core.exception.BizException;
import com.wms.common.core.result.R;
import com.wms.common.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 认证服务单元测试
 *
 * <p>覆盖测试点：TP-R1-1.1.1-01 ~ TP-R1-1.1.1-10、菜单与刷新 Token
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String USERNAME = "admin";
    private static final String RAW_PASSWORD = "123456";
    private static final String ENCODED_PASSWORD = "{noop}123456";
    private static final Long USER_ID = 1L;
    private static final String IP = "127.0.0.1";

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private PermissionFeignClient permissionFeignClient;

    @Mock
    private LoginLogFeignClient loginLogFeignClient;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private LoginGuard loginGuard;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Spy
    private PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "passwordMinLength", 8);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /* ==================== 登录 ==================== */

    @Test
    @DisplayName("登录成功：返回 Token 与用户信息，清除失败计数并记录日志")
    void login_shouldReturnToken_whenCredentialValid() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(false);
        when(userFeignClient.getByUsername(USERNAME)).thenReturn(R.ok(buildUser(1, 0)));
        when(permissionFeignClient.getUserPermissionCodes(USER_ID)).thenReturn(R.ok(Set.of("sku:view")));
        when(jwtTokenProvider.generateToken(any())).thenReturn("mock-token");
        when(jwtTokenProvider.getExpireSeconds()).thenReturn(28800L);

        LoginVO vo = authService.login(buildLoginDTO(RAW_PASSWORD), IP);

        assertThat(vo.getToken()).isEqualTo("mock-token");
        assertThat(vo.getUserId()).isEqualTo(USER_ID);
        assertThat(vo.getUsername()).isEqualTo(USERNAME);
        assertThat(vo.getRoleCode()).isEqualTo("SYSTEM_ADMIN");
        assertThat(vo.getForceChangePassword()).isFalse();
        verify(loginGuard).clearFail(USERNAME);
        verify(loginLogFeignClient).record(any());
    }

    @Test
    @DisplayName("用户不存在：抛出 10001 并记录失败日志")
    void login_shouldThrow_whenUserNotExists() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(false);
        when(userFeignClient.getByUsername(USERNAME)).thenReturn(R.ok(null));

        BizException ex = assertThrows(BizException.class,
                () -> authService.login(buildLoginDTO(RAW_PASSWORD), IP));

        assertThat(ex.getCode()).isEqualTo(10001);
        verify(loginLogFeignClient).record(any());
    }

    @Test
    @DisplayName("账户已锁定：抛出 10003，不查询用户")
    void login_shouldThrow_whenAccountLocked() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(true);

        BizException ex = assertThrows(BizException.class,
                () -> authService.login(buildLoginDTO(RAW_PASSWORD), IP));

        assertThat(ex.getCode()).isEqualTo(10003);
        verify(userFeignClient, never()).getByUsername(any());
    }

    @Test
    @DisplayName("账户已停用：抛出 10002")
    void login_shouldThrow_whenAccountDisabled() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(false);
        when(userFeignClient.getByUsername(USERNAME)).thenReturn(R.ok(buildUser(0, 0)));

        BizException ex = assertThrows(BizException.class,
                () -> authService.login(buildLoginDTO(RAW_PASSWORD), IP));

        assertThat(ex.getCode()).isEqualTo(10002);
    }

    @Test
    @DisplayName("密码错误：抛出 10001 并提示剩余次数")
    void login_shouldThrowWithRemain_whenPasswordWrong() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(false);
        when(userFeignClient.getByUsername(USERNAME)).thenReturn(R.ok(buildUser(1, 0)));
        when(loginGuard.recordFail(USERNAME)).thenReturn(4);

        BizException ex = assertThrows(BizException.class,
                () -> authService.login(buildLoginDTO("wrong-password"), IP));

        assertThat(ex.getCode()).isEqualTo(10001);
        assertThat(ex.getMessage()).contains("剩余 4 次");
        verify(loginGuard).recordFail(USERNAME);
    }

    @Test
    @DisplayName("首次登录（强制改密标记）：返回 forceChangePassword=true")
    void login_shouldMarkForceChangePassword_whenFirstLogin() {
        when(loginGuard.isLocked(USERNAME)).thenReturn(false);
        when(userFeignClient.getByUsername(USERNAME)).thenReturn(R.ok(buildUser(1, 1)));
        when(permissionFeignClient.getUserPermissionCodes(USER_ID)).thenReturn(R.ok(Set.of()));
        when(jwtTokenProvider.generateToken(any())).thenReturn("mock-token");
        when(jwtTokenProvider.getExpireSeconds()).thenReturn(28800L);

        LoginVO vo = authService.login(buildLoginDTO(RAW_PASSWORD), IP);

        assertThat(vo.getForceChangePassword()).isTrue();
    }

    /* ==================== 修改密码 ==================== */

    @Test
    @DisplayName("两次新密码不一致：抛出 400")
    void changePassword_shouldThrow_whenConfirmNotMatch() {
        ChangePasswordRequest request = buildChangeRequest("old12345", "Abcd1234", "Abcd9999");

        BizException ex = assertThrows(BizException.class,
                () -> authService.changePassword(USER_ID, request));

        assertThat(ex.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("新密码长度不足：抛出 10006")
    void changePassword_shouldThrow_whenPasswordTooShort() {
        ChangePasswordRequest request = buildChangeRequest("old12345", "Ab1", "Ab1");

        BizException ex = assertThrows(BizException.class,
                () -> authService.changePassword(USER_ID, request));

        assertThat(ex.getCode()).isEqualTo(10006);
    }

    @Test
    @DisplayName("新密码缺少字母或数字：抛出 10007")
    void changePassword_shouldThrow_whenPasswordMissingLetterOrDigit() {
        ChangePasswordRequest request = buildChangeRequest("old12345", "12345678", "12345678");

        BizException ex = assertThrows(BizException.class,
                () -> authService.changePassword(USER_ID, request));

        assertThat(ex.getCode()).isEqualTo(10007);
    }

    @Test
    @DisplayName("原密码错误：远端返回失败时抛出 10005")
    void changePassword_shouldThrow_whenOldPasswordIncorrect() {
        ChangePasswordRequest request = buildChangeRequest("wrong-old", "Abcd1234", "Abcd1234");
        when(userFeignClient.changePassword(eq(USER_ID), any(ChangePasswordDTO.class)))
                .thenReturn(R.fail(10005, "原密码错误"));

        BizException ex = assertThrows(BizException.class,
                () -> authService.changePassword(USER_ID, request));

        assertThat(ex.getCode()).isEqualTo(10005);
    }

    @Test
    @DisplayName("修改密码成功：调用远端接口")
    void changePassword_shouldSuccess_whenValid() {
        ChangePasswordRequest request = buildChangeRequest("old12345", "Abcd1234", "Abcd1234");
        when(userFeignClient.changePassword(eq(USER_ID), any(ChangePasswordDTO.class))).thenReturn(R.ok());

        authService.changePassword(USER_ID, request);

        verify(userFeignClient).changePassword(eq(USER_ID), any(ChangePasswordDTO.class));
    }

    /* ==================== 菜单与刷新 ==================== */

    @Test
    @DisplayName("查询菜单：返回菜单树与权限集合")
    void menus_shouldReturnMenuAndPermissions() {
        when(permissionFeignClient.getUserPermissionCodes(USER_ID)).thenReturn(R.ok(Set.of("sku:view")));
        when(permissionFeignClient.getUserMenus(USER_ID))
                .thenReturn(R.ok(List.of(new PermissionNodeDTO())));

        TokenVO vo = authService.menus(USER_ID);

        assertThat(vo.getPermissions()).containsExactly("sku:view");
        assertThat(vo.getMenus()).hasSize(1);
    }

    @Test
    @DisplayName("刷新 Token：生成新 Token 并使旧 Token 失效")
    void refresh_shouldGenerateNewToken_andBlacklistOld() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn(String.valueOf(USER_ID));
        when(claims.get(JwtTokenProvider.CLAIM_USERNAME, String.class)).thenReturn(USERNAME);
        when(jwtTokenProvider.parseToken("old-token")).thenReturn(claims);
        when(permissionFeignClient.getUserPermissionCodes(USER_ID)).thenReturn(R.ok(Set.of()));
        when(jwtTokenProvider.generateToken(any())).thenReturn("new-token");
        when(jwtTokenProvider.getExpireSeconds()).thenReturn(28800L);
        when(jwtTokenProvider.getJti("old-token")).thenReturn("jti-old");
        when(jwtTokenProvider.getExpiration("old-token")).thenReturn(System.currentTimeMillis() + 3_600_000);

        TokenVO vo = authService.refresh("old-token");

        assertThat(vo.getToken()).isEqualTo("new-token");
        verify(valueOperations).set(eq("wms:auth:token:blacklist:jti-old"), any(), any());
    }

    @Test
    @DisplayName("退出登录：Token 为空时直接返回，不写黑名单")
    void logout_shouldReturn_whenTokenEmpty() {
        authService.logout(null);

        verify(redisTemplate, never()).opsForValue();
    }

    /* ==================== 私有辅助 ==================== */

    private LoginDTO buildLoginDTO(String password) {
        LoginDTO dto = new LoginDTO();
        dto.setUsername(USERNAME);
        dto.setPassword(password);
        return dto;
    }

    private UserAuthDTO buildUser(Integer status, Integer forceChangePassword) {
        UserAuthDTO user = new UserAuthDTO();
        user.setUserId(USER_ID);
        user.setUsername(USERNAME);
        user.setRealName("系统管理员");
        user.setPassword(ENCODED_PASSWORD);
        user.setRoleId(1L);
        user.setRoleCode("SYSTEM_ADMIN");
        user.setWarehouseId(null);
        user.setStatus(status);
        user.setForceChangePassword(forceChangePassword);
        return user;
    }

    private ChangePasswordRequest buildChangeRequest(String oldPwd, String newPwd, String confirmPwd) {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword(oldPwd);
        request.setNewPassword(newPwd);
        request.setConfirmPassword(confirmPwd);
        return request;
    }
}
