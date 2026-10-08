package com.wms.common.security.jwt;

import com.wms.common.security.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 令牌工具
 *
 * @author WMS
 */
@Slf4j
@Component
public class JwtTokenProvider {

    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_REAL_NAME = "realName";
    public static final String CLAIM_ROLE_CODE = "roleCode";
    public static final String CLAIM_WAREHOUSE_ID = "warehouseId";

    @Value("${wms.jwt.secret:wms-default-jwt-secret-key-please-change-in-production-environment}")
    private String secret;

    @Value("${wms.jwt.expire-seconds:28800}")
    private long expireSeconds;

    /**
     * 生成 Token
     *
     * @param loginUser 登录用户信息
     * @return JWT 字符串
     */
    public String generateToken(LoginUser loginUser) {
        Map<String, Object> claims = new HashMap<>(4);
        claims.put(CLAIM_USERNAME, loginUser.getUsername());
        claims.put(CLAIM_REAL_NAME, loginUser.getRealName());
        claims.put(CLAIM_ROLE_CODE, loginUser.getRoleCode());
        claims.put(CLAIM_WAREHOUSE_ID, loginUser.getWarehouseId());
        return buildToken(String.valueOf(loginUser.getUserId()), claims);
    }

    /**
     * 生成 Token（自定义 jti）
     *
     * @param subject 用户 ID
     * @param claims  自定义声明
     * @return JWT 字符串
     */
    public String buildToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expireSeconds * 1000);
        return Jwts.builder()
                .id(UUID.randomUUID().toString().replace("-", ""))
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(getSecretKey())
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT 字符串
     * @return 声明集合
     * @throws JwtException Token 非法或过期
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验 Token 是否有效
     *
     * @param token JWT 字符串
     * @return true 有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token 校验失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取 jti（用于 Token 黑名单）
     *
     * @param token JWT 字符串
     * @return jti
     */
    public String getJti(String token) {
        return parseToken(token).getId();
    }

    /**
     * 获取过期时间（毫秒时间戳）
     *
     * @param token JWT 字符串
     * @return 过期时间戳
     */
    public long getExpiration(String token) {
        return parseToken(token).getExpiration().getTime();
    }

    /**
     * Token 有效期（秒）
     *
     * @return 有效期秒数
     */
    public long getExpireSeconds() {
        return expireSeconds;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
