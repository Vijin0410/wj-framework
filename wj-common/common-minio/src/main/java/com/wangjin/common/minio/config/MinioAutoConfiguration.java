package com.wangjin.common.minio.config;

import com.wangjin.common.minio.controller.MinioFileController;
import com.wangjin.common.minio.service.MinioService;
import com.wangjin.common.minio.service.MinioServiceImpl;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * common-minio 自动装配。
 * <ul>
 *   <li>{@code wj.minio.enabled=true} + endpoint/密钥 → 装配 Client / Service</li>
 *   <li>{@code wj.minio.endpoint-enabled=true} → 额外注册通用上传 HTTP</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(prefix = "wj.minio", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MinioAutoConfiguration.class);

    private static final String SECURITY_UTILS = "com.wangjin.common.security.util.SecurityUtils";

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient(MinioProperties properties) {
        if (!StringUtils.hasText(properties.getEndpoint())) {
            throw new IllegalStateException("wj.minio.enabled=true 时必须配置 wj.minio.endpoint；不需要时请设置 enabled=false");
        }
        if (!StringUtils.hasText(properties.getAccessKey()) || !StringUtils.hasText(properties.getSecretKey())) {
            throw new IllegalStateException("wj.minio.access-key / secret-key 未配置");
        }
        log.info("Init MinioClient endpoint={}, bucket={}", properties.getEndpoint(), properties.getBucket());
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioServiceImpl.TenantIdProvider minioTenantIdProvider() {
        return () -> {
            try {
                Class<?> utils = Class.forName(SECURITY_UTILS);
                Object id = utils.getMethod("getTenantId").invoke(null);
                return id instanceof Long l ? l : null;
            } catch (ClassNotFoundException | NoClassDefFoundError e) {
                return null;
            } catch (ReflectiveOperationException e) {
                log.debug("解析租户 ID 失败，objectKey 将不带租户前缀: {}", e.toString());
                return null;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean(MinioService.class)
    public MinioService minioService(MinioClient minioClient,
                                     MinioProperties properties,
                                     MinioServiceImpl.TenantIdProvider tenantIdProvider) {
        return new MinioServiceImpl(minioClient, properties, tenantIdProvider);
    }

    /**
     * 内置上传 HTTP；仅 Web 应用且 endpoint-enabled=true。
     */
    @AutoConfiguration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnProperty(prefix = "wj.minio", name = "endpoint-enabled", havingValue = "true")
    @ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
    @Import(MinioFileController.class)
    static class MinioEndpointConfiguration {
    }
}
