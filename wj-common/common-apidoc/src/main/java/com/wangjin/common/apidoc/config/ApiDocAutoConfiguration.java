package com.wangjin.common.apidoc.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * common-apidoc 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(OpenAPI.class)
@Import(OpenApiConfig.class)
public class ApiDocAutoConfiguration {
}
