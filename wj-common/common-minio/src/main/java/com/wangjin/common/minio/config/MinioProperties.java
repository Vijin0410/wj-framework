package com.wangjin.common.minio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * MinIO 配置。
 * <pre>
 * wj:
 *   minio:
 *     enabled: true
 *     endpoint: http://127.0.0.1:9000
 *     access-key: minioadmin
 *     secret-key: minioadmin
 *     bucket: hair-salon
 *     public-url: http://127.0.0.1:9000
 *     endpoint-enabled: true   # 可选内置上传 HTTP
 * </pre>
 */
@ConfigurationProperties(prefix = "wj.minio")
public class MinioProperties {

    /**
     * 总开关。默认 false：引入依赖后须显式 enabled=true 并配置 endpoint。
     */
    private boolean enabled = false;

    /** MinIO / S3 endpoint，例如 http://127.0.0.1:9000 */
    private String endpoint;

    private String accessKey;

    private String secretKey;

    /** 默认桶名 */
    private String bucket = "wj-default";

    /**
     * 对外访问基址（CDN / 反代 / 外网）。
     * 空则回落 endpoint。拼 URL：{publicUrl}/{bucket}/{objectKey}
     */
    private String publicUrl;

    /** 预签名 URL 默认有效期（秒），上限 7 天 */
    private int defaultExpirySeconds = 3600;

    /** 启动时桶不存在则创建 */
    private boolean createBucketIfMissing = true;

    /**
     * 是否写入公开读策略（Logo / 商品图等）。
     * 私有桶保持 false，用预签名访问。
     */
    private boolean publicRead = true;

    /** 允许的扩展名（小写、无点）。空列表表示不校验。 */
    private List<String> allowedExtensions = new ArrayList<>(List.of(
            "png", "jpg", "jpeg", "gif", "webp", "bmp", "svg",
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "zip", "rar", "7z"
    ));

    /** 单文件最大字节；0 表示本模块不校验 */
    private long maxSize = 10 * 1024 * 1024L;

    /** 对象 key 是否自动加租户前缀（有 common-security 时生效） */
    private boolean tenantPath = true;

    /**
     * 是否注册框架内置上传 HTTP（{@code /api/v1/files/**}）。
     * 默认 false：只提供 Service，由业务自己写 Controller。
     * 通用场景可开 true，配合 {@link #endpointRequireAuth} / 业务 Security。
     */
    private boolean endpointEnabled = false;

    /**
     * 内置接口路径前缀（不含尾斜杠）
     */
    private String endpointBasePath = "/api/v1/files";

    /**
     * 内置接口是否要求已登录（反射调用 SecurityContext；无 security 时忽略）。
     * 生产建议 true，或业务侧用 Security 拦截该路径。
     */
    private boolean endpointRequireAuth = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public void setPublicUrl(String publicUrl) {
        this.publicUrl = publicUrl;
    }

    public int getDefaultExpirySeconds() {
        return defaultExpirySeconds;
    }

    public void setDefaultExpirySeconds(int defaultExpirySeconds) {
        this.defaultExpirySeconds = defaultExpirySeconds;
    }

    public boolean isCreateBucketIfMissing() {
        return createBucketIfMissing;
    }

    public void setCreateBucketIfMissing(boolean createBucketIfMissing) {
        this.createBucketIfMissing = createBucketIfMissing;
    }

    public boolean isPublicRead() {
        return publicRead;
    }

    public void setPublicRead(boolean publicRead) {
        this.publicRead = publicRead;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(long maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isTenantPath() {
        return tenantPath;
    }

    public void setTenantPath(boolean tenantPath) {
        this.tenantPath = tenantPath;
    }

    public boolean isEndpointEnabled() {
        return endpointEnabled;
    }

    public void setEndpointEnabled(boolean endpointEnabled) {
        this.endpointEnabled = endpointEnabled;
    }

    public String getEndpointBasePath() {
        return endpointBasePath;
    }

    public void setEndpointBasePath(String endpointBasePath) {
        this.endpointBasePath = endpointBasePath;
    }

    public boolean isEndpointRequireAuth() {
        return endpointRequireAuth;
    }

    public void setEndpointRequireAuth(boolean endpointRequireAuth) {
        this.endpointRequireAuth = endpointRequireAuth;
    }
}
