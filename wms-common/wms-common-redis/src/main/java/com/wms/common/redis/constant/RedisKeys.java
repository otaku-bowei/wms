package com.wms.common.redis.constant;

/**
 * Redis Key 常量
 *
 * <p>规范：{系统}:{模块}:{业务}:{标识}
 *
 * @author WMS
 */
public final class RedisKeys {

    private static final String PREFIX = "wms:";

    private RedisKeys() {
    }

    /** 用户权限集合 */
    public static String userPerms(Long userId) {
        return PREFIX + "auth:perms:" + userId;
    }

    /** Token 黑名单 */
    public static String tokenBlacklist(String jti) {
        return PREFIX + "auth:token:blacklist:" + jti;
    }

    /** 登录失败次数 */
    public static String loginFail(String username) {
        return PREFIX + "auth:login:fail:" + username;
    }

    /** 账户锁定 */
    public static String loginLock(String username) {
        return PREFIX + "auth:login:lock:" + username;
    }

    /** 字典缓存 */
    public static String dict(String dictType) {
        return PREFIX + "sys:dict:" + dictType;
    }

    /** 系统参数缓存 */
    public static String config(String paramKey) {
        return PREFIX + "sys:config:" + paramKey;
    }

    /** 编码流水号 */
    public static String seq(String ruleType, String datePart) {
        return PREFIX + "seq:" + ruleType + ":" + datePart;
    }

    /** 分布式锁 */
    public static String lock(String bizKey) {
        return PREFIX + "lock:" + bizKey;
    }

    /** 幂等键 */
    public static String idempotent(String key) {
        return PREFIX + "idempotent:" + key;
    }
}
