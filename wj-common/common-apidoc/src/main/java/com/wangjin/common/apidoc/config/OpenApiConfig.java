package com.wangjin.common.apidoc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

/**
 * OpenAPI / Knife4j 配置（Bearer JWT）。
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ApiDocProperties.class)
public class OpenApiConfig {

    private final ApiDocProperties properties;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(properties.getTitle())
                        .description(properties.getDescription())
                        .version(properties.getVersion())
                        .contact(new Contact()
                                .name(properties.getContactName())
                                .email(properties.getContactEmail())))
                .components(new Components()
                        .addSecuritySchemes(HttpHeaders.AUTHORIZATION,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name(HttpHeaders.AUTHORIZATION)))
                .addSecurityItem(new SecurityRequirement().addList(HttpHeaders.AUTHORIZATION));
    }
}
