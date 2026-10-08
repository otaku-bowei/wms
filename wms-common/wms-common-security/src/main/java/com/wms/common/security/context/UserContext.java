package com.wms.common.security.context;

/**
 * 用户上下文（ThreadLocal）
 *
 * @author WMS
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static String getUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static String getRoleCode() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getRoleCode();
    }

    public static Long getWarehouseId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getWarehouseId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
