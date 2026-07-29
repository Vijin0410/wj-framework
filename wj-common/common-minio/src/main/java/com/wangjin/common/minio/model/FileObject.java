package com.wangjin.common.minio.model;

/**
 * 上传结果 / 对象描述。业务表建议持久化 {@link #objectKey}，展示用 {@link #url}。
 */
public class FileObject {

    /** 桶内对象键，例如 1/avatar/20260729/a1b2c3d4.png */
    private String objectKey;

    private String bucket;

    private String originalFilename;

    /** 扩展名（无点，小写） */
    private String extension;

    private String contentType;

    private long size;

    /** 可读大小，如 1.2 MB */
    private String sizeText;

    /** 对外访问 URL */
    private String url;

    public String getObjectKey() {
        return objectKey;
    }

    public FileObject setObjectKey(String objectKey) {
        this.objectKey = objectKey;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public FileObject setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public FileObject setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
        return this;
    }

    public String getExtension() {
        return extension;
    }

    public FileObject setExtension(String extension) {
        this.extension = extension;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public FileObject setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public long getSize() {
        return size;
    }

    public FileObject setSize(long size) {
        this.size = size;
        return this;
    }

    public String getSizeText() {
        return sizeText;
    }

    public FileObject setSizeText(String sizeText) {
        this.sizeText = sizeText;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public FileObject setUrl(String url) {
        this.url = url;
        return this;
    }
}
