package com.wangjin.common.mybatis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * MyBatis / 多租户配置。
 */
@Data
@ConfigurationProperties(prefix = "wj.mybatis")
public class MybatisProperties {

    /** 数据库类型，见 DbType 名 */
    private String dbType = "postgresql";

    /** 是否启用租户行拦截 */
    private boolean tenantEnabled = true;

    /** 租户字段名 */
    private String tenantColumn = "tenant_id";

    /**
     * 忽略租户过滤的表（关联表、全局字典等）。
     */
    private List<String> ignoreTables = new ArrayList<>(List.of(
            "sys_tenant",
            "sys_user_role",
            "sys_role_menu"
    ));

    /** 是否启用数据权限拦截 */
    private boolean dataPermissionEnabled = true;
}
