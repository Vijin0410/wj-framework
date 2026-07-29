# MinIO 本地部署（Windows Docker）与业务接入

开发机：Windows + Docker Desktop。生产：同一套镜像与配置思路，改 host/域名/密钥即可。

## 1. 前置

- 已安装 [Docker Desktop](https://www.docker.com/products/docker-desktop/)，Linux 容器模式
- 终端能执行 `docker version`、`docker compose version`

## 2. 一键启动

```bash
cd wj-framework/docs
docker compose -f minio-docker-compose.yml up -d
```

| 地址 | 用途 |
|------|------|
| http://127.0.0.1:9000 | S3 API（应用连接） |
| http://127.0.0.1:9001 | Web 控制台 |

默认账号（**仅开发**）：`minioadmin` / `minioadmin`  
数据目录：`wj-framework/docs/minio-data/`（已 gitignore）

```bash
docker compose -f minio-docker-compose.yml logs -f minio
docker compose -f minio-docker-compose.yml stop
docker compose -f minio-docker-compose.yml down
```

## 3. 控制台建桶（可选）

应用侧默认 `create-bucket-if-missing=true`，一般不用手建。

## 4. 业务项目配置

### 4.1 Maven

```xml
<dependency>
  <groupId>com.wangjin</groupId>
  <artifactId>common-minio</artifactId>
</dependency>
```

### 4.2 application.yml

```yaml
wj:
  minio:
    enabled: true
    endpoint: http://127.0.0.1:9000
    access-key: minioadmin
    secret-key: minioadmin
    bucket: hair-salon
    public-url: http://127.0.0.1:9000
    create-bucket-if-missing: true
    public-read: true
    tenant-path: true
    max-size: 10485760
    # 通用上传 HTTP：需要则 true，自建 Controller 则 false
    endpoint-enabled: true
    endpoint-require-auth: true
    # endpoint-base-path: /api/v1/files
```

### 4.3 代码

**只调 Service：**

```java
FileObject obj = minioService.upload(file, "logo");
// 库里存 obj.getObjectKey()；展示用 obj.getUrl()
```

**或开内置 HTTP：**

```http
POST /api/v1/files/upload
Content-Type: multipart/form-data
Authorization: Bearer ...

file: (binary)
biz: logo
```

对象 key：`{tenantId}/{biz}/{yyyyMMdd}/{uuid}.ext`

## 5. 生产（Linux）

- 改掉默认 root 账号密码
- `public-url` 用 HTTPS / CDN
- 数据卷可靠磁盘 + 备份
- 密钥走环境变量，勿提交仓库

## 6. 与 zhyinfo-minio

| 项 | zhyinfo-minio | wj common-minio |
|----|---------------|-----------------|
| 形态 | 独立微服务 + Feign + 文档库表 | 库内自动配置 |
| 能力 | 目录/权限/文档元数据/PDF | 上传/删/URL + 可选通用 HTTP |
| SDK | 7.0.2 旧 API | 8.x |

**不要**把 zhyinfo-minio 服务迁进业务单体。
