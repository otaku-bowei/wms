package com.wms.auth.guard;

import com.wms.common.redis.constant.RedisKeys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 登录防护器单元测试
 *
 * <p>覆盖测试点：TP-R1-1.1.1-04 ~ TP-R1-1.1.1-07（失败计数、账户锁定、计数清除）
 *
 * @author WMS
 */
@ExtendWith(MockitoExtension.class)
class LoginGuardTest {

    private static final String USERNAME = "admin";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private LoginGuard loginGuard;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginGuard, "maxFailCount", 5);
        ReflectionTestUtils.setField(loginGuard, "lockMinutes", 30L);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("账户已锁定时 isLocked 返回 true")
    void isLocked_shouldReturnTrue_whenLockKeyExists() {
        when(redisTemplate.hasKey(RedisKeys.loginLock(USERNAME))).thenReturn(true);

        assertThat(loginGuard.isLocked(USERNAME)).isTrue();
    }

    @Test
    @DisplayName("账户未锁定时 isLocked 返回 false")
    void isLocked_shouldReturnFalse_whenLockKeyNotExists() {
        when(redisTemplate.hasKey(RedisKeys.loginLock(USERNAME))).thenReturn(false);

        assertThat(loginGuard.isLocked(USERNAME)).isFalse();
    }

    @Test
    @DisplayName("记录失败未达阈值：返回剩余次数，不锁定")
    void recordFail_shouldReturnRemain_whenBelowThreshold() {
        when(valueOperations.increment(RedisKeys.loginFail(USERNAME))).thenReturn(2L);

        int remain = loginGuard.recordFail(USERNAME);

        assertThat(remain).isEqualTo(3);
        verify(valueOperations).increment(RedisKeys.loginFail(USERNAME));
    }

    @Test
    @DisplayName("首次失败：设置失败计数过期时间")
    void recordFail_shouldSetExpire_whenFirstFail() {
        when(valueOperations.increment(RedisKeys.loginFail(USERNAME))).thenReturn(1L);

        int remain = loginGuard.recordFail(USERNAME);

        assertThat(remain).isEqualTo(4);
        verify(redisTemplate).expire(RedisKeys.loginFail(USERNAME), Duration.ofMinutes(30));
    }

    @Test
    @DisplayName("失败次数达到阈值：返回 0 并写入锁定 Key")
    void recordFail_shouldLock_whenReachThreshold() {
        when(valueOperations.increment(RedisKeys.loginFail(USERNAME))).thenReturn(5L);

        int remain = loginGuard.recordFail(USERNAME);

        assertThat(remain).isZero();
        verify(valueOperations).set(eq(RedisKeys.loginLock(USERNAME)), anyString(), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("清除失败计数：删除 Redis 计数 Key")
    void clearFail_shouldDeleteFailKey() {
        loginGuard.clearFail(USERNAME);

        verify(redisTemplate).delete(RedisKeys.loginFail(USERNAME));
    }

    @Test
    @DisplayName("计算剩余尝试次数")
    void remainAttempts_shouldCalculateRemain() {
        assertThat(loginGuard.remainAttempts(0)).isEqualTo(5);
        assertThat(loginGuard.remainAttempts(3)).isEqualTo(2);
        assertThat(loginGuard.remainAttempts(5)).isZero();
        assertThat(loginGuard.remainAttempts(9)).isZero();
    }
}
