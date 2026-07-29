package com.wangjin.common.minio.util;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.wangjin.common.exception.BizException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对象 key 与扩展名校验（借鉴 zhyinfo-minio 的 uuid 改名 + 白名单思路）。
 */
public final class MinioPathHelper {

    private static final DateTimeFormatter DAY = DateTimeFormatter.BASIC_ISO_DATE;

    private MinioPathHelper() {
    }

    /**
     * 生成对象 key：[{tenantId}/]{biz}/{yyyyMMdd}/{uuid}[.{ext}]
     */
    public static String buildObjectKey(Long tenantId, String biz, String originalFilename, boolean tenantPath) {
        String safeBiz = sanitizeSegment(StrUtil.blankToDefault(biz, "common"));
        String ext = extensionOf(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String name = StrUtil.isBlank(ext) ? uuid : uuid + "." + ext;
        String day = LocalDate.now().format(DAY);

        if (tenantPath && tenantId != null) {
            return tenantId + "/" + safeBiz + "/" + day + "/" + name;
        }
        return safeBiz + "/" + day + "/" + name;
    }

    public static String extensionOf(String originalFilename) {
        if (StrUtil.isBlank(originalFilename)) {
            return "";
        }
        String ext = FileNameUtil.extName(originalFilename);
        return ext == null ? "" : ext.toLowerCase(Locale.ROOT);
    }

    public static void assertAllowed(String extension, List<String> allowed) {
        if (allowed == null || allowed.isEmpty()) {
            return;
        }
        Set<String> set = allowed.stream()
                .filter(StrUtil::isNotBlank)
                .map(s -> s.toLowerCase(Locale.ROOT).replace(".", ""))
                .collect(Collectors.toSet());
        String ext = extension == null ? "" : extension.toLowerCase(Locale.ROOT);
        if (StrUtil.isBlank(ext) || !set.contains(ext)) {
            throw new BizException("不允许的文件类型: " + (StrUtil.blankToDefault(ext, "(无扩展名)")));
        }
    }

    public static void assertMaxSize(long size, long maxSize) {
        if (maxSize > 0 && size > maxSize) {
            throw new BizException("文件过大，最大允许 " + maxSize + " 字节");
        }
    }

    /** 路径段清洗，避免 ../ 与非法字符。 */
    public static String sanitizeSegment(String segment) {
        String s = segment.trim().replace('\\', '/');
        while (s.startsWith("/")) {
            s = s.substring(1);
        }
        if (s.contains("..") || s.contains("/")) {
            throw new BizException("非法业务路径: " + segment);
        }
        String cleaned = s.replaceAll("[^a-zA-Z0-9._-]", "_");
        if (StrUtil.isBlank(cleaned)) {
            return "common";
        }
        return cleaned;
    }
}
