package com.wangjin.common.mybatis.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;

/**
 * common-mybatis 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(MybatisPlusInterceptor.class)
@Import(MybatisPlusConfig.class)
public class MybatisAutoConfiguration {
}
