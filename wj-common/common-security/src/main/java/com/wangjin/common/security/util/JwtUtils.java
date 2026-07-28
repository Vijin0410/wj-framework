package com.wangjin.common.security.util;

import com.wangjin.common.constant.SecurityConstants;
import com.wangjin.common.security.context.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT 工具（jjwt 0.12.x）。
 */
public final class JwtUtils {

    private JwtUtils() {
    }

    public static String createToken(LoginUser user, String secret, long expireSeconds) {
        Map<String, Object> claims = new HashMap<>(12);
        claims.put(SecurityConstants.DETAILS_USER_ID, user.getUserId());
        claims.put(SecurityConstants.DETAILS_USERNAME, user.getUsername());
        claims.put(SecurityConstants.DETAILS_NICKNAME, user.getNickname());
        claims.put(SecurityConstants.DETAILS_TENANT_ID, user.getTenantId());
        claims.put(SecurityConstants.DETAILS_DEPT_ID, user.getDeptId());
        claims.put(SecurityConstants.DETAILS_DATA_SCOPE, user.getDataScope());
        claims.put(SecurityConstants.DETAILS_DATA_SCOPE_DEPTS, user.getDataScopeDeptIds());
        claims.put(SecurityConstants.DETAILS_ROLES, user.getRoles());
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
        user.setDataScope(toInteger(claims.get(SecurityConstants.DETAILS_DATA_SCOPE)));
        user.setDataScopeDeptIds(toLongSet(claims.get(SecurityConstants.DETAILS_DATA_SCOPE_DEPTS)));
        user.setRoles(toStringSet(claims.get(SecurityConstants.DETAILS_ROLES)));
        user.setPermissions(toStringSet(claims.get(SecurityConstants.AUTHORITIES_CLAIM_NAME)));
        user.setTokenId(claims.getId());
        return user;
    }

    public static boolean isExpired(Claims claims) {
        Date exp = claims.getExpiration();
        return exp != null && exp.before(new Date());
    }

    private static SecretKey secretKey(String secret) {
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

    private static Integer toInteger(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<String> toStringSet(Object val) {
        if (val == null) {
            return Collections.emptySet();
        }
        if (val instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).collect(Collectors.toCollection(HashSet::new));
        }
        return new HashSet<>(List.of(String.valueOf(val)));
    }

    @SuppressWarnings("unchecked")
    private static Set<Long> toLongSet(Object val) {
        if (val == null) {
            return Collections.emptySet();
        }
        if (val instanceof Collection<?> c) {
            return c.stream().map(JwtUtils::toLong).filter(v -> v != null).collect(Collectors.toCollection(HashSet::new));
        }
        Long one = toLong(val);
        return one == null ? Collections.emptySet() : new HashSet<>(List.of(one));
    }

    private static String asString(Object val) {
        return val == null ? null : val.toString();
    }
}
