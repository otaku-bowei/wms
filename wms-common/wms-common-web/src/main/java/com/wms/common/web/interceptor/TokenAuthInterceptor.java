package com.wms.common.web.interceptor;

import com.wms.common.redis.constant.RedisKeys;
import com.wms.common.security.context.LoginUser;
import com.wms.common.security.context.UserContext;
import com.wms.common.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

/**
 * Token 认证拦截器（通用）
 *
 * <p>解析 Authorization 中的 JWT，填充 {@link UserContext}，供各微服务复用。
 *
 * @author WMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenAuthInterceptor implements HandlerInterceptor {

    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token)) {
            return true;
        }
        try {
            Claims claims = jwtTokenProvider.parseToken(token);
            LoginUser loginUser = new LoginUser();
            loginUser.setUserId(Long.valueOf(claims.getSubject()));
            loginUser.setUsername(claims.get(JwtTokenProvider.CLAIM_USERNAME, String.class));
            loginUser.setRealName(claims.get(JwtTokenProvider.CLAIM_REAL_NAME, String.class));
            loginUser.setRoleCode(claims.get(JwtTokenProvider.CLAIM_ROLE_CODE, String.class));
            Object warehouseId = claims.get(JwtTokenProvider.CLAIM_WAREHOUSE_ID);
            loginUser.setWarehouseId(warehouseId == null ? null : Long.valueOf(String.valueOf(warehouseId)));
            loginUser.setPermissions(loadPermissions(loginUser.getUserId()));
            UserContext.set(loginUser);
        } catch (Exception e) {
            log.debug("解析 Token 失败：{}", e.getMessage());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /**
     * 从 Authorization 头中解析 Token
     *
     * @param authorization 请求头值
     * @return Token，为空返回 null
     */
    public static String resolveToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return authorization;
    }

    private Set<String> loadPermissions(Long userId) {
        try {
            String cached = redisTemplate.opsForValue().get(RedisKeys.userPerms(userId));
            if (StringUtils.hasText(cached)) {
                return Set.of(cached.split(","));
            }
        } catch (Exception e) {
            log.debug("读取权限缓存失败：userId={}", userId);
        }
        return Set.of();
    }
}
