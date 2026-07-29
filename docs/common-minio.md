# common-minio

MinIO 对象存储封装。按需依赖，**不进入** `wj-starter-boot` 默认套餐。

## 能力

### Service（始终可注入，在 `wj.minio.enabled=true` 时）

- 上传（`MultipartFile` / `InputStream`）
- 删除（单个 / 批量）
- 公有 URL、预签名 URL
- 流式下载、存在性检查
- 启动自动建桶；可选公开读策略
- 可选租户路径前缀

### 可选 HTTP（`wj.minio.endpoint-enabled=true`）

| 方法 | 路径（默认前缀 `/api/v1/files`） | 说明 |
|------|----------------------------------|------|
| POST | `/upload` | 单文件，`file` + 可选 `biz` |
| POST | `/upload/batch` | 多文件 |
| DELETE | `?objectKey=` | 删除 |
| GET | `/url?objectKey=` | 公有 URL |
| GET | `/presign?objectKey=&expirySeconds=` | 预签名 |

- 默认 **不注册** HTTP，避免业务想自己控权限时被框架接口干扰
- `endpoint-require-auth=true`（默认）时反射校验 Spring Security 已登录；细粒度 `hasAuthority` 请业务自建或配 Security
- 路径可用 `wj.minio.endpoint-base-path` 改

## 依赖

```xml
<dependency>
  <groupId>com.wangjin</groupId>
  <artifactId>common-minio</artifactId>
</dependency>
```

## 配置

见 [minio-deploy.md](minio-deploy.md)。前缀：`wj.minio.*`。

```yaml
wj:
  minio:
    enabled: true
    endpoint: http://127.0.0.1:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: hair-salon
    public-url: http://127.0.0.1:9000
    # 需要通用上传接口时打开
    endpoint-enabled: true
    endpoint-require-auth: true
```

## 包结构

```text
com.wangjin.common.minio
├── config   MinioProperties / MinioAutoConfiguration
├── model    FileObject
├── service  MinioService / MinioServiceImpl
├── util     MinioPathHelper
└── controller MinioFileController   # 可选
```

## 设计取舍

- 模块名 **common-minio**：当前唯一实现是 MinIO；若将来多厂商再抽 SPI
- 配置前缀 **wj.minio**，与实现一致
- **无**文档表 / 目录树 / 文档权限
- 业务表存 `objectKey`，展示用 `url` 或预签名
