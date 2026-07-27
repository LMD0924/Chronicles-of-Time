/**
 * 文件说明：拾光记微服务后端公共核心通用工具源码，负责通用工具相关的接口、业务、数据或配置逻辑，保持各微服务边界清晰。
 */
package org.example.commoncore.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * 类说明：当前类是通用工具模块的组成部分，与控制层、服务层、数据层或配置层协作，保障拾光记业务闭环可维护。
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKeyForJWTGenerationWithEnoughLength12345678}")
    private String secret;

    @Value("${jwt.expiration:3600000}")
    private Long expiration;

    @Value("${jwt.refreshExpiration:604800000}")
    private Long refreshExpiration;

    /**
     * 根据配置中的密钥生成 HMAC 签名 Key，所有服务必须共享同一密钥才能完成网关验签。
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String username, Long userId, String role) {
        Set<String> roles = new LinkedHashSet<>();
        if (role != null && !role.isBlank()) {
            roles.add(role);
        }
        return generateAccessToken(username, userId, roles, List.of());
    }

    /**
     * 生成访问令牌，角色和按钮权限会写入 claims，便于前端和网关统一做 RBAC 判断。
     */
    public String generateAccessToken(String username, Long userId, Collection<String> roles, Collection<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = cleanList(roles);
        claims.put("userId", userId);
        claims.put("role", roleList.isEmpty() ? "USER" : roleList.get(0));
        claims.put("roles", roleList.isEmpty() ? List.of("USER") : roleList);
        claims.put("permissions", cleanList(permissions));
        claims.put("type", "access");
        return createToken(claims, username, expiration);
    }

    public String generateRefreshToken(String username, Long userId) {
        return generateRefreshToken(username, userId, UUID.randomUUID().toString());
    }

    public String generateRefreshToken(String username, Long userId, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, username, refreshExpiration, sessionId);
    }
    public String generateAccessTokenWithRememberMe(String username, Long userId, String role, Boolean rememberMe) {
        Set<String> roles = new LinkedHashSet<>();
        if (role != null && !role.isBlank()) {
            roles.add(role);
        }
        return generateAccessTokenWithRememberMe(username, userId, roles, List.of(), rememberMe);
    }

    public String generateAccessTokenWithRememberMe(String username, Long userId, Collection<String> roles,
                                                    Collection<String> permissions, Boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = cleanList(roles);
        claims.put("userId", userId);
        claims.put("role", roleList.isEmpty() ? "USER" : roleList.get(0));
        claims.put("roles", roleList.isEmpty() ? List.of("USER") : roleList);
        claims.put("permissions", cleanList(permissions));
        claims.put("type", "access");
        Long expireTime = Boolean.TRUE.equals(rememberMe) ? refreshExpiration : expiration;
        return createToken(claims, username, expireTime);
    }

    public String generateAccessTokenWithRememberMe(String username, Long userId, Collection<String> roles,
                                                    Collection<String> permissions, Boolean rememberMe, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = cleanList(roles);
        claims.put("userId", userId);
        claims.put("role", roleList.isEmpty() ? "USER" : roleList.get(0));
        claims.put("roles", roleList.isEmpty() ? List.of("USER") : roleList);
        claims.put("permissions", cleanList(permissions));
        claims.put("type", "access");
        Long expireTime = Boolean.TRUE.equals(rememberMe) ? refreshExpiration : expiration;
        return createToken(claims, username, expireTime, sessionId);
    }

    public String generateAccessToken(String username, Long userId, Collection<String> roles,
                                      Collection<String> permissions, String sessionId) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roleList = cleanList(roles);
        claims.put("userId", userId);
        claims.put("role", roleList.isEmpty() ? "USER" : roleList.get(0));
        claims.put("roles", roleList.isEmpty() ? List.of("USER") : roleList);
        claims.put("permissions", cleanList(permissions));
        claims.put("type", "access");
        return createToken(claims, username, expiration, sessionId);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime) {
        return createToken(claims, subject, expirationTime, UUID.randomUUID().toString());
    }

    private String createToken(Map<String, Object> claims, String subject, Long expirationTime, String tokenId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(tokenId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 校验 JWT 格式、签名和过期时间。
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("Token expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Malformed token: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Invalid token signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid token argument: {}", e.getMessage());
        }
        return false;
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Long getUserIdFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        String role = claims.get("role", String.class);
        if (role != null) {
            return role;
        }
        List<String> roles = getRolesFromToken(token);
        return roles.isEmpty() ? "USER" : roles.get(0);
    }

    public List<String> getRolesFromToken(String token) {
        return getStringListClaim(token, "roles");
    }

    public List<String> getPermissionsFromToken(String token) {
        return getStringListClaim(token, "permissions");
    }

    public Boolean isRefreshToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return "refresh".equals(claims.get("type"));
    }

    public Boolean isAccessToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        return "access".equals(claims.get("type"));
    }

    public String getTokenId(String token) {
        return getAllClaimsFromToken(token).getId();
    }
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public Long getRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(0, remaining);
        } catch (Exception e) {
            log.error("Failed to get remaining token time: {}", e.getMessage());
            return 0L;
        }
    }



    /**
     * 兼容单字符串和数组两种 claims 写法，避免历史 token 或不同服务写入格式不一致。
     */
    private List<String> getStringListClaim(String token, String key) {
        Object value = getAllClaimsFromToken(token).get(key);
        if (value instanceof Collection<?> values) {
            return values.stream()
                    .filter(item -> item != null && !item.toString().isBlank())
                    .map(Object::toString)
                    .distinct()
                    .toList();
        }
        if (value instanceof String text && !text.isBlank()) {
            return List.of(text);
        }
        return List.of();
    }

    private List<String> cleanList(Collection<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return values.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
