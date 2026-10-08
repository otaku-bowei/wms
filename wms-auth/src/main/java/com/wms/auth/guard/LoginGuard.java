package com.wms.auth.guard;

import com.wms.common.redis.constant.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 登录防护器
 *
 * <p>基于 Redis 实现：连续登录失败计数、达到阈值后锁定账户。
 *
 * @author WMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginGuard {

    private final StringRedisTemplate redisTemplate;

    /** 允许的最大连续失败次数 */
    @Value("${wms.login.max-fail-count:5}")
    private int maxFailCount;

    /** 锁定时长（分钟） */
    @Value("${wms.login.lock-minutes:30}")
    private long lockMinutes;

    /**
     * 账户是否已锁定
     *
     * @param username 用户名
     * @return true 已锁定
     */
    public boolean isLocked(String username) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.loginLock(username)));
    }

    /**
     * 记录一次登录失败，返回剩余可尝试次数
     *
     * @param username 用户名
     * @return 剩余次数（0 表示已锁定）
     */
    public int recordFail(String username) {
        String failKey = RedisKeys.loginFail(username);
        Long count = redisTemplate.opsForValue().increment(failKey);
        long current = count == null ? 1 : count;
        if (current == 1) {
            redisTemplate.expire(failKey, Duration.ofMinutes(lockMinutes));
        }
        if (current >= maxFailCount) {
            redisTemplate.opsForValue()
                    .set(RedisKeys.loginLock(username), String.valueOf(System.currentTimeMillis()), Duration.ofMinutes(lockMinutes));
            log.warn("账户[{}]连续失败{}次，已锁定{}分钟", username, current, lockMinutes);
            return 0;
        }
        return remainAttempts(current);
    }

    /**
     * 清除失败计数（登录成功后调用）
     *
     * @param username 用户名
     */
    public void clearFail(String username) {
        redisTemplate.delete(RedisKeys.loginFail(username));
    }

    /**
     * 计算剩余尝试次数
     *
     * @param failCount 已失败次数
     * @return 剩余次数
     */
    public int remainAttempts(long failCount) {
        return (int) Math.max(0, maxFailCount - failCount);
    }
}
