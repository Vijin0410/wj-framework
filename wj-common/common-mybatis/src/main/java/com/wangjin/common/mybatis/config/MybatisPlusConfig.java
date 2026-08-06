package com.wangjin.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.wangjin.common.mybatis.handler.MyMetaObjectHandler;
import com.wangjin.common.mybatis.handler.WjDataPermissionHandler;
import com.wangjin.common.mybatis.handler.WjTenantLineHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus：租户行 → 数据权限 → 分页 + 自动填充。
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(MybatisProperties.class)
@RequiredArgsConstructor
public class MybatisPlusConfig {

    private final MybatisProperties properties;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 乐观锁支持
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        // 1. 多租户（先于数据权限）
        if (properties.isTenantEnabled()) {
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new WjTenantLineHandler(properties)));
        }

        // 2. 数据权限
        if (properties.isDataPermissionEnabled()) {
            interceptor.addInnerInterceptor(new DataPermissionInterceptor(new WjDataPermissionHandler()));
        }

        // 3. 分页
        DbType type;
        try {
            type = DbType.getDbType(properties.getDbType());
        } catch (Exception e) {
            type = DbType.POSTGRE_SQL;
        }
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(type));
        return interceptor;
    }

    @Bean
    public GlobalConfig globalConfig(MyMetaObjectHandler metaObjectHandler) {
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setMetaObjectHandler(metaObjectHandler);
        return globalConfig;
    }

    @Bean
    public MyMetaObjectHandler myMetaObjectHandler() {
        return new MyMetaObjectHandler();
    }
}
