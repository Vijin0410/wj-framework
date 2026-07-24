package com.wangjin.common.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * common-security 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(HttpSecurity.class)
@Import(SecurityConfig.class)
public class SecurityAutoConfiguration {
}
