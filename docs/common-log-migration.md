# common-log / logback 说明

## 1. 两类「日志」不要混

| 类型 | 模块 | 作用 |
|------|------|------|
| **运行日志** | `wj-starter-boot` 的 `logback-spring.xml` | 框架/业务 `log.info` 输出到控制台与文件 |
| **操作日志（审计）** | `common-log` 的 `@Log` + 切面 | 记录「谁在什么接口做了什么」 |

上家把 Logback 放在 `zhyinfo-starter-boot`，操作日志切面放在 `common-log`——我们保持同样拆分。

---

## 2. `@Log` 使用说明

### 2.1 引入依赖

使用 `wj-starter-boot` 时已包含 `common-log`；单独引用：

```xml
<dependency>
    <groupId>com.wangjin</groupId>
    <artifactId>common-log</artifactId>
</dependency>
```

### 2.2 注解属性

| 属性 | 类型 | 默认 | 说明 |
|------|------|------|------|
| `title` | String | `""` | 操作标题，如「用户管理-新增」 |
| `businessType` | BusinessType | `OTHER` | 业务类型：SELECT / INSERT / UPDATE / DELETE / GRANT / EXPORT / IMPORT 等 |
| `operatorType` | OperatorType | `MANAGE` | 操作端：OTHER / MANAGE（后台）/ MOBILE（移动端） |
| `isSaveRequestData` | boolean | `true` | 是否记录请求参数 |
| `isSaveResponseData` | boolean | `true` | 是否记录响应结果 |
| `excludeParamNames` | String[] | `{}` | 额外排除的参数字段名（会与内置 password 等合并脱敏） |
| `bizNo` | String | `""` | 业务主键 SpEL，如 `#id`、`#form.userId` |

可标在**方法**或**类**上；方法注解优先于类注解。

### 2.3 基本用法

```java
@RestController
@RequestMapping("/users")
public class UserController {

    @Log(title = "用户管理-新增", businessType = BusinessType.INSERT)
    @PostMapping
    public Result<Void> create(@RequestBody @Valid UserForm form) {
        userService.create(form);
        return Result.success();
    }

    @Log(title = "用户管理-修改", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody UserForm form) {
        userService.update(id, form);
        return Result.success();
    }

    @Log(title = "用户管理-删除", businessType = BusinessType.DELETE, bizNo = "#id")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Log(title = "用户管理-导出", businessType = BusinessType.EXPORT, isSaveResponseData = false)
    @GetMapping("/export")
    public void export(UserQuery query, HttpServletResponse response) {
        userService.export(query, response);
    }
}
```

类级别统一标题/类型（方法可再覆盖）：

```java
@Log(title = "用户管理", operatorType = OperatorType.MANAGE)
@RestController
@RequestMapping("/users")
public class UserController { ... }
```

### 2.4 内置脱敏字段

记录请求参数时，以下字段名（不区分大小写）会脱敏为 `***`：

`password`、`oldPassword`、`newPassword`、`confirmPassword`、`pwd`、`secret`、`token`、`accessToken`、`refreshToken`

额外排除：

```java
@Log(title = "更新资料", excludeParamNames = {"idCard", "bankNo"})
```

### 2.5 日志写到哪里（OperLogHandler）

切面只负责**采集**，组装 `OperLog` 后：

1. 若容器中有 `OperLogHandler` Bean → 回调业务处理  
2. 否则 → 打一条 `info` 运行日志（不丢信息，也不强迫中间件）

**入库示例：**

```java
@Component
@RequiredArgsConstructor
public class DbOperLogHandler implements OperLogHandler {

    private final OperLogService operLogService;

    @Override
    public void handle(OperLog operLog) {
        // 映射到自己的 oper_log 表
        operLogService.save(operLog);
    }
}
```

**发 MQ 示例（对齐上家行为）：**

```java
@Component
@RequiredArgsConstructor
public class MqOperLogHandler implements OperLogHandler {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void handle(OperLog operLog) {
        rabbitTemplate.convertAndSend("YOUR-TOPIC", "operateLog.log", operLog);
    }
}
```

`OperLog` 主要字段：`title`、`businessType`、`method`、`requestMethod`、`operatorName`、`operateUrl`、`operateIp`、`requestParam`、`jsonResult`、`status`、`errorMsg`、`costTime`、`bizNo`、`createBy` 等。

### 2.6 注意点

- 切面异常不会影响主业务（内部 catch 后只 warn）  
- 未登录也会记日志（`operatorName` 可能为空）  
- 参数/响应默认截断约 2000 字符  
- `MultipartFile`、`HttpServletRequest` 等不会序列化进参数  

---

## 3. Logback 运行日志

| 上家 | wj-framework |
|------|----------------|
| `logback.xml` | `logback-spring.xml`（支持 `springProperty`） |
| 硬编码 `/logs/zhyinfo` | `${logging.file.path}`，默认 `logs` |
| 文件名写死 `zhyinfo.log` | `${spring.application.name}.log` |
| 仅同步 RollingFile | 增加 AsyncAppender |

业务覆盖：自备 `logback-spring.xml`，或改 yml：

```yaml
logging:
  file:
    path: /var/log/myapp
  level:
    com.wangjin: debug
spring:
  application:
    name: my-service   # 日志文件名 my-service.log
```

---

## 4. 与上家 common-log 差异

| 点 | zhyinfo | wj-framework | 原因 |
|----|---------|--------------|------|
| **投递方式** | 切面内 `RabbitTemplate` → 固定 Topic | `OperLogHandler` SPI | 框架不绑 MQ |
| **标题 title** | `@Tag` + `@Operation` 拼接 | `@Log(title=...)` | 不强制 Swagger |
| **businessType** | 方法名模糊推断 | 注解声明 | 更准确 |
| **无登录** | operatorName 空则不记 | 仍记录 | 匿名/任务也要审计 |
| **JSON** | Fastjson | Jackson | 与 Boot 3 一致 |
| **敏感字段** | Fastjson Filter | Filter + 字符串脱敏 | 能力对齐 |
| **bizNo** | SpEL | SpEL（`@Log.bizNo`） | 对齐 |
| **枚举存储** | 多用 ordinal | 存 name 字符串 | 避免顺序变更踩坑 |

---

## 5. 相关文件

```text
wj-starter-boot/src/main/resources/
  logback-spring.xml
  application.yml

wj-common/common-log/
  annotation/Log.java
  aspect/LogAspect.java
  filter/SensitivePropertyFilter.java
  model/OperLog.java
  model/OperLogHandler.java
  enums/*
  config/LogAutoConfiguration.java
```
