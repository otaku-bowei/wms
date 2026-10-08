package com.wms.gateway.filter;

import com.wms.common.redis.constant.RedisKeys;
import com.wms.common.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 网关鉴权过滤器
 *
 * <p>校验 JWT 有效性、Token 黑名单，并将用户信息透传给下游服务。
 *
 * @author WMS
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";

    /** 白名单路径（无需鉴权） */
    private static final List<String> WHITE_LIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    );

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_ROLE_CODE = "X-Role-Code";
    private static final String HEADER_WAREHOUSE_ID = "X-Warehouse-Id";

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单放行
        if (isWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String token = resolveToken(request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (!StringUtils.hasText(token)) {
            return unauthorized(exchange, "未提供认证 Token");
        }

        // 3. 校验 Token
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(token);
        } catch (Exception e) {
            log.debug("Token 校验失败：{}", e.getMessage());
            return unauthorized(exchange, "Token 无效或已过期");
        }

        // 4. 黑名单校验（登出的 Token）
        String jti = claims.getId();
        if (StringUtils.hasText(jti) && Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeys.tokenBlacklist(jti)))) {
            return unauthorized(exchange, "Token 已失效，请重新登录");
        }

        // 5. 透传用户信息
        ServerHttpRequest mutated = request.mutate()
                .header(HEADER_USER_ID, String.valueOf(claims.getSubject()))
                .header(HEADER_USER_NAME, str(claims.get(JwtTokenProvider.CLAIM_USERNAME)))
                .header(HEADER_ROLE_CODE, str(claims.get(JwtTokenProvider.CLAIM_ROLE_CODE)))
                .header(HEADER_WAREHOUSE_ID, str(claims.get(JwtTokenProvider.CLAIM_WAREHOUSE_ID)))
                .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 100;
    }

    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    private String resolveToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith(BEARER_PREFIX)) {
            return authorization.substring(BEARER_PREFIX.length());
        }
        return authorization;
    }

    private String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"code\":401,\"message\":\"" + message + "\",\"data\":null,"
                + "\"timestamp\":" + System.currentTimeMillis() + "}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
