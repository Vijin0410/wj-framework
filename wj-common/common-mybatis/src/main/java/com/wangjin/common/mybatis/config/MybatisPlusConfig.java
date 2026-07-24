package com.wangjin.common.mybatis.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.wangjin.common.mybatis.handler.MyMetaObjectHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * MyBatis-Plus 配置：分页 + 自动填充。
 */
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {

    /** 数据库类型，默认 MySQL，可通过 wj.mybatis.db-type 覆盖 */
    @Value("${wj.mybatis.db-type:MYSQL}")
    private String dbType;

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DbType type;
        try {
            type = DbType.getDbType(dbType);
        } catch (Exception e) {
            type = DbType.MYSQL;
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
