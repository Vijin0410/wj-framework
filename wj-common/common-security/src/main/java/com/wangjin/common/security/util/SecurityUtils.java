package com.wangjin.common.security.util;

import com.wangjin.common.constant.GlobalConstants;
import com.wangjin.common.security.context.LoginUser;
import com.wangjin.common.security.context.UserContext;

import java.util.Collections;
import java.util.Set;

/**
 * 安全工具门面（兼容上家 SecurityUtils 用法）。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Long getUserId() {
        return UserContext.getUserId();
    }

    public static String getUsername() {
        return UserContext.getUsername();
    }

    public static String getNickname() {
        return UserContext.getNickname();
    }

    public static Long getTenantId() {
        return UserContext.getTenantId();
    }

    public static Long getDeptId() {
        return UserContext.getDeptId();
    }

    public static String getJti() {
        return UserContext.getTokenId();
    }

    public static Set<String> getRoles() {
        LoginUser user = UserContext.get();
        return user == null || user.getRoles() == null ? Collections.emptySet() : user.getRoles();
    }

    public static boolean isRoot() {
        return getRoles().stream().anyMatch(r ->
                GlobalConstants.ROOT_ROLE_CODE.equalsIgnoreCase(r));
    }
}
