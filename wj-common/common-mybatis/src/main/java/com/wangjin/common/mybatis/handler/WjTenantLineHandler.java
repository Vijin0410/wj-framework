package com.wangjin.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.wangjin.common.mybatis.config.MybatisProperties;
import com.wangjin.common.security.util.SecurityUtils;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * 租户行处理器：所有未忽略表自动拼 tenant_id = 当前租户。
 * <p>
 * 管理员也走租户过滤（单租户下即默认租户 1 的全量数据）；
 * 跨租户超管若以后需要，可在此对 ROOT 返回 ignore。
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
        return ignoreTables.contains(tableName.toLowerCase(Locale.ROOT));
    }
}
