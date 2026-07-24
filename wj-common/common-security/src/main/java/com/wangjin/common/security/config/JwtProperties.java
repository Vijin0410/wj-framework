package com.wangjin.common.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置项。
 *
 * <pre>
 * wj:
 *   security:
 *     jwt:
 *       secret: your-256bit-secret-key-change-me!!
 *       expire-seconds: 7200
 *       header: Authorization
 * </pre>
 */
@Data
@ConfigurationProperties(prefix = "wj.security.jwt")
public class JwtProperties {

    /** HMAC 密钥，长度建议 >= 32 字节 */
    private String secret = "wj-framework-default-secret-key-32b";

    /** 过期秒数，默认 2 小时 */
    private long expireSeconds = 7200;

    /** 请求头名称 */
    private String header = "Authorization";

    /** 放行路径 */
    private String[] ignoreUrls = {
            "/auth/login",
            "/auth/register",
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/favicon.ico",
            "/error"
    };
}
