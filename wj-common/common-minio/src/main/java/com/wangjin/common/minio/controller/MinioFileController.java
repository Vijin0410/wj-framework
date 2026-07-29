package com.wangjin.common.minio.controller;

import com.wangjin.common.exception.BizException;
import com.wangjin.common.minio.config.MinioProperties;
import com.wangjin.common.minio.model.FileObject;
import com.wangjin.common.minio.service.MinioService;
import com.wangjin.common.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 可选通用上传 HTTP。默认不注册；{@code wj.minio.endpoint-enabled=true} 时生效。
 * <p>
 * 路径默认 {@code /api/v1/files/**}，可用 {@code wj.minio.endpoint-base-path} 覆盖。
 * 鉴权：优先依赖业务 Security 拦截该路径；模块内 {@code endpoint-require-auth=true} 时额外校验已登录。
 * 细粒度 perm（如 system:file:upload）请业务自建 Controller 或在 Security 中配置。
 */
@RestController
@RequestMapping("${wj.minio.endpoint-base-path:/api/v1/files}")
@ConditionalOnProperty(prefix = "wj.minio", name = "endpoint-enabled", havingValue = "true")
public class MinioFileController {

    private static final Logger log = LoggerFactory.getLogger(MinioFileController.class);

    private final MinioService minioService;
    private final MinioProperties properties;

    public MinioFileController(MinioService minioService, MinioProperties properties) {
        this.minioService = minioService;
        this.properties = properties;
    }

    /**
     * 单文件上传。
     *
     * @param file 文件
     * @param biz  业务目录，默认 common
     */
    @PostMapping("/upload")
    public Result<FileObject> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "biz", required = false, defaultValue = "common") String biz) {
        assertAuthIfRequired();
        return Result.success(minioService.upload(file, biz));
    }

    /**
     * 多文件上传。
     */
    @PostMapping("/upload/batch")
    public Result<List<FileObject>> uploadBatch(@RequestParam("file") MultipartFile[] files,
                                                @RequestParam(value = "biz", required = false, defaultValue = "common") String biz) {
        assertAuthIfRequired();
        if (files == null || files.length == 0) {
            throw new BizException("上传文件不能为空");
        }
        List<FileObject> list = Arrays.stream(files)
                .filter(Objects::nonNull)
                .filter(f -> !f.isEmpty())
                .map(f -> minioService.upload(f, biz))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        return Result.success(list);
    }

    /**
     * 按 objectKey 删除。
     */
    @DeleteMapping
    public Result<Void> delete(@RequestParam("objectKey") String objectKey) {
        assertAuthIfRequired();
        minioService.delete(objectKey);
        return Result.success();
    }

    /**
     * 取公有 URL（不鉴权对象是否可读，仅拼路径）。
     */
    @GetMapping("/url")
    public Result<String> publicUrl(@RequestParam("objectKey") String objectKey) {
        assertAuthIfRequired();
        return Result.success(minioService.getPublicUrl(objectKey));
    }

    /**
     * 预签名下载 URL。
     *
     * @param expirySeconds 可选，默认配置值
     */
    @GetMapping("/presign")
    public Result<String> presign(@RequestParam("objectKey") String objectKey,
                                  @RequestParam(value = "expirySeconds", required = false, defaultValue = "0") int expirySeconds) {
        assertAuthIfRequired();
        return Result.success(minioService.getPresignedUrl(objectKey, expirySeconds));
    }

    private void assertAuthIfRequired() {
        if (!properties.isEndpointRequireAuth()) {
            return;
        }
        try {
            Class<?> holder = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = holder.getMethod("getContext").invoke(null);
            Object auth = context.getClass().getMethod("getAuthentication").invoke(context);
            if (auth == null) {
                throw new BizException("未登录");
            }
            Boolean authenticated = (Boolean) auth.getClass().getMethod("isAuthenticated").invoke(auth);
            String name = String.valueOf(auth.getClass().getMethod("getName").invoke(auth));
            if (!Boolean.TRUE.equals(authenticated) || "anonymousUser".equals(name)) {
                throw new BizException("未登录");
            }
        } catch (BizException e) {
            throw e;
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            log.debug("无 Spring Security，跳过 endpoint 登录校验");
        } catch (ReflectiveOperationException e) {
            throw new BizException("鉴权校验失败");
        }
    }
}
