package com.wangjin.common.apidoc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 接口文档配置。
 *
 * <pre>
 * wj:
 *   apidoc:
 *     title: WangJin API
 *     description: 接口文档
 *     version: 1.0.0
 *     contact-name: wangjin
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "wj.apidoc")
public class ApiDocProperties {

    private String title = "WangJin API";
    private String description = "API Documentation";
    private String version = "1.0.0";
    private String contactName = "wangjin";
    private String contactEmail = "";
}
