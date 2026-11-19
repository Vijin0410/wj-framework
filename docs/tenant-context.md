# 租户操作上下文

`TenantContext` 提供当前线程的临时租户作用域，与 `LoginUser` 身份分离。框架不解析租户请求头，不查询租户业务表，也不决定哪个角色可以切换租户。

接入项目完成身份授权和租户有效性校验后，可使用：

```java
try (TenantContext.Scope scope = TenantContext.use(tenantId)) {
    // 查询、更新、删除由 TenantLine 添加租户条件。
    // 新增记录的 tenantId 自动填充使用相同租户，createBy 仍为实际登录用户。
}
```

作用域支持嵌套，关闭时恢复外层租户，异常退出也必须关闭。HTTP 集成应在请求完成及异步交接时释放作用域；JWT 过滤器在请求退出时额外清理，避免线程复用残留。上下文不会自动传播到线程池、定时任务或异步回调，这些入口须自行校验并建立作用域。

显式作用域对 ROOT 同样执行租户隔离。没有显式作用域时保留现有行为：普通身份使用登录租户，ROOT 跨租户；没有登录信息则回落默认租户。全局表仍由 `wj.mybatis.ignore-tables` 配置，显式忽略 TenantLine 的 Mapper 仍须由接入项目保证查询范围。

`SecurityUtils.getTenantId()` 返回当前操作租户；`UserContext.getTenantId()` 保留登录身份的原始租户。业务缓存应使用操作租户作为键的一部分。通用字典翻译在作用域内叠加对应租户的字典覆盖。
