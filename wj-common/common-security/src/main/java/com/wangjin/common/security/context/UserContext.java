package com.wangjin.common.security.context;

/**
 * 登录用户 ThreadLocal 上下文。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null || user.getUserId() == null ? 0L : user.getUserId();
    }

    public static String getUsername() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUsername();
    }

    public static String getNickname() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getNickname();
    }

    public static Long getTenantId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getTenantId();
    }

    public static Long getDeptId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getDeptId();
    }

    public static String getTokenId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getTokenId();
    }
}
