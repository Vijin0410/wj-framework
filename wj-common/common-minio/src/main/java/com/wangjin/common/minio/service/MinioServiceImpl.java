package com.wangjin.common.minio.service;

import cn.hutool.core.io.unit.DataSizeUtil;
import cn.hutool.core.util.StrUtil;
import com.wangjin.common.exception.BizException;
import com.wangjin.common.minio.config.MinioProperties;
import com.wangjin.common.minio.model.FileObject;
import com.wangjin.common.minio.util.MinioPathHelper;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.StatObjectArgs;
import io.minio.http.Method;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 实现。API 对齐官方 8.x。
 */
public class MinioServiceImpl implements MinioService, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(MinioServiceImpl.class);

    private static final int MAX_EXPIRY_SECONDS = 7 * 24 * 3600;

    private final MinioClient minioClient;
    private final MinioProperties properties;
    private final TenantIdProvider tenantIdProvider;

    public MinioServiceImpl(MinioClient minioClient,
                            MinioProperties properties,
                            TenantIdProvider tenantIdProvider) {
        this.minioClient = minioClient;
        this.properties = properties;
        this.tenantIdProvider = tenantIdProvider;
    }

    @Override
    public void afterPropertiesSet() {
        initBucket();
    }

    public void initBucket() {
        if (!properties.isCreateBucketIfMissing()) {
            return;
        }
        String bucket = properties.getBucket();
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("MinIO bucket created: {}", bucket);
            }
            if (properties.isPublicRead()) {
                applyPublicReadPolicy(bucket);
            }
        } catch (Exception e) {
            throw new BizException("初始化 MinIO 桶失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileObject upload(MultipartFile file, String biz) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        try {
            return upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    biz
            );
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("读取上传文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public FileObject upload(InputStream inputStream, String originalFilename, String contentType, long size, String biz) {
        if (inputStream == null) {
            throw new BizException("上传流不能为空");
        }
        String ext = MinioPathHelper.extensionOf(originalFilename);
        MinioPathHelper.assertAllowed(ext, properties.getAllowedExtensions());
        if (size >= 0) {
            MinioPathHelper.assertMaxSize(size, properties.getMaxSize());
        }

        Long tenantId = properties.isTenantPath() ? tenantIdProvider.currentTenantId() : null;
        String objectKey = MinioPathHelper.buildObjectKey(tenantId, biz, originalFilename, properties.isTenantPath());
        String bucket = properties.getBucket();
        String ct = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";

        try {
            long objectSize = size >= 0 ? size : -1;
            long partSize = objectSize < 0 ? 10 * 1024 * 1024L : -1;
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(inputStream, objectSize, partSize)
                    .contentType(ct)
                    .build());
        } catch (Exception e) {
            throw new BizException("上传失败: " + StrUtil.blankToDefault(originalFilename, objectKey) + " — " + e.getMessage(), e);
        }

        long finalSize = size >= 0 ? size : 0;
        return new FileObject()
                .setObjectKey(objectKey)
                .setBucket(bucket)
                .setOriginalFilename(originalFilename)
                .setExtension(ext)
                .setContentType(ct)
                .setSize(finalSize)
                .setSizeText(finalSize > 0 ? DataSizeUtil.format(finalSize) : null)
                .setUrl(getPublicUrl(objectKey));
    }

    @Override
    public void delete(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            return;
        }
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new BizException("删除文件失败: " + objectKey, e);
        }
    }

    @Override
    public void delete(Collection<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) {
            return;
        }
        List<DeleteObject> objects = new ArrayList<>();
        for (String key : objectKeys) {
            if (StrUtil.isNotBlank(key)) {
                objects.add(new DeleteObject(key));
            }
        }
        if (objects.isEmpty()) {
            return;
        }
        try {
            Iterable<io.minio.Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(properties.getBucket())
                            .objects(objects)
                            .build());
            for (io.minio.Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.warn("MinIO 批量删除失败 object={}, message={}", error.objectName(), error.message());
            }
        } catch (Exception e) {
            throw new BizException("批量删除文件失败", e);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            return null;
        }
        String base = StrUtil.blankToDefault(properties.getPublicUrl(), properties.getEndpoint());
        if (StrUtil.isBlank(base)) {
            return objectKey;
        }
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String key = objectKey.startsWith("/") ? objectKey.substring(1) : objectKey;
        return base + "/" + properties.getBucket() + "/" + key;
    }

    @Override
    public String getPresignedUrl(String objectKey, int expirySeconds) {
        if (StrUtil.isBlank(objectKey)) {
            throw new BizException("objectKey 不能为空");
        }
        int expiry = expirySeconds > 0 ? expirySeconds : properties.getDefaultExpirySeconds();
        if (expiry < 1 || expiry > MAX_EXPIRY_SECONDS) {
            throw new BizException("预签名有效期须在 1 ~ " + MAX_EXPIRY_SECONDS + " 秒");
        }
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .expiry(expiry, TimeUnit.SECONDS)
                    .build());
        } catch (Exception e) {
            throw new BizException("生成预签名 URL 失败: " + objectKey, e);
        }
    }

    @Override
    public InputStream download(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            throw new BizException("objectKey 不能为空");
        }
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new BizException("下载文件失败: " + objectKey, e);
        }
    }

    @Override
    public boolean exists(String objectKey) {
        if (StrUtil.isBlank(objectKey)) {
            return false;
        }
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(properties.getBucket())
                    .object(objectKey)
                    .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getBucket() {
        return properties.getBucket();
    }

    private void applyPublicReadPolicy(String bucket) {
        String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {"AWS": ["*"]},
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucket);
        try {
            minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(bucket)
                    .config(policy)
                    .build());
            log.info("MinIO bucket public-read policy applied: {}", bucket);
        } catch (Exception e) {
            log.warn("设置桶公开读策略失败（可忽略，改用预签名）: {}", e.getMessage());
        }
    }

    @FunctionalInterface
    public interface TenantIdProvider {
        Long currentTenantId();
    }
}
