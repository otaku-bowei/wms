package com.wms.common.redis.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁（基于 Redisson）
 *
 * @author WMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLock {

    /** 默认等待时间（秒） */
    private static final long DEFAULT_WAIT_SECONDS = 5L;

    /** 默认持有时间（秒） */
    private static final long DEFAULT_LEASE_SECONDS = 30L;

    private final RedissonClient redissonClient;

    /**
     * 加锁执行（无返回值）
     *
     * @param key   业务锁 key
     * @param task  执行逻辑
     */
    public void lockAndRun(String key, Runnable task) {
        lockAndRun(key, DEFAULT_WAIT_SECONDS, DEFAULT_LEASE_SECONDS, task);
    }

    /**
     * 加锁执行（无返回值，自定义超时）
     *
     * @param key          业务锁 key
     * @param waitSeconds  等待时间
     * @param leaseSeconds 持有时间
     * @param task         执行逻辑
     */
    public void lockAndRun(String key, long waitSeconds, long leaseSeconds, Runnable task) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("获取分布式锁失败：" + key);
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取分布式锁被中断：" + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 加锁执行（带返回值）
     *
     * @param key    业务锁 key
     * @param action 执行逻辑
     * @param <T>    返回值类型
     * @return 执行结果
     */
    public <T> T lockAndGet(String key, Supplier<T> action) {
        return lockAndGet(key, DEFAULT_WAIT_SECONDS, DEFAULT_LEASE_SECONDS, action);
    }

    /**
     * 加锁执行（带返回值，自定义超时）
     *
     * @param key          业务锁 key
     * @param waitSeconds  等待时间
     * @param leaseSeconds 持有时间
     * @param action       执行逻辑
     * @param <T>          返回值类型
     * @return 执行结果
     */
    public <T> T lockAndGet(String key, long waitSeconds, long leaseSeconds, Supplier<T> action) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("获取分布式锁失败：" + key);
            }
            return action.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取分布式锁被中断：" + key, e);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
