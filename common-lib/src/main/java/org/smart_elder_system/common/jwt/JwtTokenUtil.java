package org.smart_elder_system.common.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 统一的JWT令牌工具类
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取声明
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            Duration clockSkew = jwtProperties.getClockSkew() != null ? jwtProperties.getClockSkew() : Duration.ofSeconds(30);
            long clockSkewSeconds = clockSkew.getSeconds();
            
            log.debug("解析JWT令牌 - 时钟偏移容忍度: {}秒", clockSkewSeconds);
            
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .clockSkewSeconds(clockSkewSeconds)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.error("JWT令牌已过期: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的JWT令牌: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.error("JWT令牌格式错误: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT令牌参数错误: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        try {
            // 尝试获取所有声明，如果令牌已过期，会抛出 ExpiredJwtException
            getAllClaimsFromToken(token);
            // 如果没有抛出异常，说明令牌未过期
            return false;
        } catch (ExpiredJwtException e) {
            log.debug("令牌已过期: {}", e.getMessage());
            return true;
        } catch (Exception e) {
            log.error("检查令牌过期状态失败: {}", e.getMessage());
            return true; // 如果无法解析，认为令牌已过期
        }
    }

    /**
     * 生成令牌
     */
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, username);
    }

    /**
     * 生成令牌（包含自定义声明）
     */
    public String generateToken(String username, Map<String, Object> claims) {
        return doGenerateToken(claims, username);
    }

    /**
     * 生成令牌的具体实现
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        long now = System.currentTimeMillis();
        long expirationTime = now + jwtProperties.getExpiration().toMillis();

        log.info("生成JWT Token - 用户: {}, 签发时间: {} ({}ms), 过期时间: {} ({}ms), 有效期: {}ms", 
                subject, 
                new Date(now), now,
                new Date(expirationTime), expirationTime,
                jwtProperties.getExpiration().toMillis());

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date(now))
                .expiration(new Date(expirationTime))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证令牌（需要UserDetails）
     */
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            // getUsernameFromToken 已经会检查令牌是否有效，如果无效会抛出异常
            return tokenUsername.equals(username);
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 验证令牌（不需要UserDetails）
     */
    public Boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            // 如果没有抛出异常，说明令牌有效
            return true;
        } catch (Exception e) {
            log.error("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取令牌过期时间（毫秒）
     */
    public Long getExpiration() {
        return jwtProperties.getExpiration().toMillis();
    }
    
    /**
     * 获取令牌的剩余有效时间（毫秒）
     * 如果令牌已过期，返回负数
     */
    public Long getRemainingTime(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            
            log.debug("计算令牌剩余时间 - 过期时间: {}, 当前时间: {}, 差值: {}ms", 
                    expiration, now, expiration.getTime() - now.getTime());
            
            return expiration.getTime() - now.getTime();
        } catch (Exception e) {
            log.error("获取令牌剩余时间失败: {}", e.getMessage());
            return -1L;
        }
    }

    /**
     * 检查令牌是否即将过期（在剩余时间的1/4内）
     */
    public Boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long timeToExpire = expiration.getTime() - System.currentTimeMillis();
            long threshold = jwtProperties.getExpiration().toMillis() / 4;
            return timeToExpire < threshold;
        } catch (Exception e) {
            return true;
        }
    }
}