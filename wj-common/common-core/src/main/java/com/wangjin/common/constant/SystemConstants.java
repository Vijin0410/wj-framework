package com.wangjin.common.constant;

/**
 * 系统管理相关常量。
 */
public final class SystemConstants {

    private SystemConstants() {
    }

    /** 根节点 ID（部门 / 菜单树） */
    public static final Long ROOT_NODE_ID = 0L;

    /** 超级管理员角色编码 */
    public static final String ROOT_ROLE_CODE = GlobalConstants.ROOT_ROLE_CODE;

    /** 新建用户默认密码 */
    public static final String DEFAULT_PASSWORD = "admin123";

    /**
     * 默认租户 ID（单租户起步；后续开通多租户时登录写入真实 tenantId）。
     */
    public static final Long DEFAULT_TENANT_ID = 1L;
}
