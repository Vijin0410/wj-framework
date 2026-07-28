# wj-framework

WangJin 底层框架（对齐 zhyinfo-framework 架构）。**类库工程，无启动类，`mvn clean install` 即可。**

## 模块结构

```text
wj-framework/
├── wj-base-dependencies/     # BOM 版本管理
├── wj-starter-boot/          # 默认依赖套餐 + logback-spring.xml + 默认 yml
└── wj-common/
    ├── common-core           # Result / BaseEntity / BaseQuery / 工具
    ├── common-web            # 全局异常 / Jackson / 校验 / CORS
    ├── common-redis          # RedisTemplate / Cache / RedisService / Redisson
    ├── common-mybatis         # 分页 / 自动填充 / BaseMapperPlus
    ├── common-security       # JWT / UserContext / SecurityFilterChain
    ├── common-mq             # RabbitMQ JSON 序列化
    ├── common-log            # @Log 操作日志（审计，非 Logback）
    └── common-apidoc         # Knife4j OpenAPI
```

操作日志迁移差异见 [docs/common-log-migration.md](docs/common-log-migration.md)。

## 迁移说明（相对 zhyinfo）

| 已迁移（通用） | 未迁移（业务耦合 / 暂不需要） |
|----------------|-------------------------------|
| Result / BaseEntity / BaseQuery | 环保站/泵房等业务 base 类 |
| 全局异常、Jackson、校验 | Feign/Nacos/Sentinel |
| Redis + Redisson | RediSearch / 地理围栏业务 |
| MP 分页 + 自动填充 | 数据权限（依赖 system-feign） |
| JWT Security + UserContext | OAuth2 授权服务器 |
| RabbitMQ 基础配置 | 短信/钉钉/验证码/ES/动态 DDL |

## 业务 model 约定

```text
entity  → 继承 BaseEntity / BaseTenantEntity
form    → 写操作入参 + @Valid
query   → 继承 BaseQuery
vo      → 出参，可继承 BaseVO
```

## 业务项目引用

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>com.wangjin</groupId>
      <artifactId>wj-base-dependencies</artifactId>
      <version>1.0.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <!-- 默认套餐：core+web+mybatis+security+redis+log+apidoc -->
  <dependency>
    <groupId>com.wangjin</groupId>
    <artifactId>wj-starter-boot</artifactId>
  </dependency>
  <!-- 按需 -->
  <dependency>
    <groupId>com.wangjin</groupId>
    <artifactId>common-mq</artifactId>
  </dependency>
</dependencies>
```

## 常用配置

```yaml
wj:
  security:
    jwt:
      secret: please-change-me-to-a-32bytes-secret!!
      expire-seconds: 7200
      ignore-urls:
        - /auth/login
        - /doc.html
        - /v3/api-docs/**
  mybatis:
    db-type: POSTGRE_SQL   # 或 MYSQL
  apidoc:
    title: My App API
    version: 1.0.0
```

## 安装

```bash
cd wj-framework
mvn clean install -DskipTests
```
