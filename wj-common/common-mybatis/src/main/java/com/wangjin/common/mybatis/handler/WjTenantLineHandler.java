package com.wangjin.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.wangjin.common.mybatis.config.MybatisProperties;
import com.wangjin.common.security.context.TenantContext;
import com.wangjin.common.security.util.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 租户行处理器：所有未忽略表自动拼 tenant_id = 当前租户。
 * <p>
 * 显式租户作用域对所有身份生效；未设置作用域时保留既有 ROOT 跨租户行为。
 */
public class WjTenantLineHandler implements TenantLineHandler {

    private final MybatisProperties properties;
    private final Set<String> ignoreTables;

    public WjTenantLineHandler(MybatisProperties properties) {
        this.properties = properties;
        this.ignoreTables = new HashSet<>();
        if (properties.getIgnoreTables() != null) {
            for (String t : properties.getIgnoreTables()) {
                if (t != null) {
                    ignoreTables.add(t.trim().toLowerCase(Locale.ROOT));
                }
            }
        }
    }

    @Override
    public Expression getTenantId() {
        return new LongValue(SecurityUtils.getTenantId());
    }

    @Override
    public String getTenantIdColumn() {
        return properties.getTenantColumn() == null ? "tenant_id" : properties.getTenantColumn();
    }

    @Override
    public boolean ignoreTable(String tableName) {
        if (!properties.isTenantEnabled()) {
            return true;
        }
        if (tableName == null) {
            return true;
        }
        // 显式作用域优先，是否允许建立作用域由接入项目授权。
        if (SecurityUtils.isRoot() && TenantContext.getTenantId() == null) {
            return true;
        }
        return ignoreTables.contains(tableName.toLowerCase(Locale.ROOT));
    }
}
