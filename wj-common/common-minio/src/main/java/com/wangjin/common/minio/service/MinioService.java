package com.wangjin.common.minio.service;

import com.wangjin.common.minio.model.FileObject;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Collection;

/**
 * MinIO 对象存储门面。业务只依赖本接口，不直接碰 SDK。
 */
public interface MinioService {

    /**
     * 上传 Multipart 文件。
     *
     * @param file 文件
     * @param biz  业务目录，如 avatar / logo / product；会进入 objectKey
     */
    FileObject upload(MultipartFile file, String biz);

    /**
     * 流式上传。
     *
     * @param inputStream      内容流（调用方关闭）
     * @param originalFilename 原始名（用于扩展名与回显）
     * @param contentType      MIME，可空
     * @param size             字节数；未知可传 -1
     * @param biz              业务目录
     */
    FileObject upload(InputStream inputStream, String originalFilename, String contentType, long size, String biz);

    /** 删除单个对象；不存在时视为成功。 */
    void delete(String objectKey);

    /** 批量删除。 */
    void delete(Collection<String> objectKeys);

    /** 公有访问 URL（path-style）。私有桶请用 {@link #getPresignedUrl}。 */
    String getPublicUrl(String objectKey);

    /**
     * 预签名下载 URL。
     *
     * @param expirySeconds 有效秒数；&lt;=0 用配置默认值
     */
    String getPresignedUrl(String objectKey, int expirySeconds);

    /** 下载对象流；调用方负责关闭。 */
    InputStream download(String objectKey);

    /** 对象是否存在。 */
    boolean exists(String objectKey);

    /** 当前默认桶。 */
    String getBucket();
}
