package com.wangjin.common.security.util;

import com.wangjin.common.constant.SecurityConstants;
import com.wangjin.common.security.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT 工具（jjwt 0.12.x）。
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    public static String createToken(LoginUser user, String secret, long expireSeconds) {
        Map<String, Object> claims = new HashMap<>(8);
        claims.put(SecurityConstants.DETAILS_USER_ID, user.getUserId());
        claims.put(SecurityConstants.DETAILS_USERNAME, user.getUsername());
        claims.put(SecurityConstants.DETAILS_NICKNAME, user.getNickname());
        claims.put(SecurityConstants.DETAILS_TENANT_ID, user.getTenantId());
        claims.put(SecurityConstants.DETAILS_DEPT_ID, user.getDeptId());
        claims.put(SecurityConstants.AUTHORITIES_CLAIM_NAME, user.getPermissions());

        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000);
        String jti = UUID.randomUUID().toString().replace("-", "");

        return Jwts.builder()
                .id(jti)
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(secretKey(secret))
                .compact();
    }

    public static Claims parseToken(String token, String secret) {
        return Jwts.parser()
                .verifyWith(secretKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static LoginUser toLoginUser(Claims claims) {
        LoginUser user = new LoginUser();
        user.setUserId(toLong(claims.get(SecurityConstants.DETAILS_USER_ID)));
        user.setUsername(asString(claims.get(SecurityConstants.DETAILS_USERNAME)));
        user.setNickname(asString(claims.get(SecurityConstants.DETAILS_NICKNAME)));
        user.setTenantId(toLong(claims.get(SecurityConstants.DETAILS_TENANT_ID)));
        user.setDeptId(toLong(claims.get(SecurityConstants.DETAILS_DEPT_ID)));
        user.setTokenId(claims.getId());
        return user;
    }

    public static boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }

    private static SecretKey secretKey(String secret) {
        // jjwt 要求 key 足够长
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static Long toLong(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number n) {
            return n.longValue();
        }
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String asString(Object val) {
        return val == null ? null : val.toString();
    }
}
