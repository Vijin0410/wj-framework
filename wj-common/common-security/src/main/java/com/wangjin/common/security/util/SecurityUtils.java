package com.wangjin.common.security.util;

import com.wangjin.common.constant.GlobalConstants;
import com.wangjin.common.constant.SystemConstants;
import com.wangjin.common.enums.DataScopeEnum;
import com.wangjin.common.security.context.LoginUser;
import com.wangjin.common.security.context.UserContext;

import java.util.Collections;
import java.util.Set;

/**
 * 安全工具门面。
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

    /**
     * 当前租户；无登录时回落默认租户（便于启动灌库 / 匿名任务）。
     */
    public static Long getTenantId() {
        Long tenantId = UserContext.getTenantId();
        return tenantId == null ? SystemConstants.DEFAULT_TENANT_ID : tenantId;
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

    public static Set<String> getPermissions() {
        LoginUser user = UserContext.get();
        return user == null || user.getPermissions() == null ? Collections.emptySet() : user.getPermissions();
    }

    public static Integer getDataScope() {
        LoginUser user = UserContext.get();
        return user == null ? null : user.getDataScope();
    }

    public static Set<Long> getDataScopeDeptIds() {
        LoginUser user = UserContext.get();
        return user == null || user.getDataScopeDeptIds() == null
                ? Collections.emptySet()
                : user.getDataScopeDeptIds();
    }

    public static boolean isRoot() {
        return getRoles().stream().anyMatch(r ->
                GlobalConstants.ROOT_ROLE_CODE.equalsIgnoreCase(r));
    }

    /**
     * 是否拥有「本租户全部数据」：ROOT 角色或 dataScope=ALL。
     */
    public static boolean isAllDataScope() {
        if (isRoot()) {
            return true;
        }
        Integer scope = getDataScope();
        return scope != null && DataScopeEnum.ALL.getValue().equals(scope);
    }
}
