package com.wms.auth.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP 工具类
 *
 * @author WMS
 */
public final class IpUtils {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "127.0.0.1";

    private IpUtils() {
    }

    /**
     * 获取客户端真实 IP
     *
     * @param request 请求
     * @return IP 地址
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return LOCALHOST;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (isInvalid(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isInvalid(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isInvalid(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return isInvalid(ip) ? LOCALHOST : ip;
    }

    private static boolean isInvalid(String ip) {
        return ip == null || ip.isBlank() || UNKNOWN.equalsIgnoreCase(ip);
    }
}
