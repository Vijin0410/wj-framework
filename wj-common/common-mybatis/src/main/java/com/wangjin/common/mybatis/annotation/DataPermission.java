package com.wangjin.common.mybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限过滤（仅 SELECT）。
 * <p>
 * 标注在 Mapper 接口或方法上；未标注则不拦截。
 * ROOT / dataScope=ALL 时不加条件（本租户全量，租户隔离仍由 TenantLine 负责）。
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface DataPermission {

    /**
     * 部门字段（相对主表别名），默认 dept_id。
     * 空字符串表示不按部门过滤。
     */
    String deptColumn() default "dept_id";

    /**
     * 用户字段（相对主表别名），默认 create_by；SELF 范围使用。
     * 空字符串表示不按用户过滤。
     */
    String userColumn() default "create_by";

    /**
     * 主表别名；空则自动取 FROM 主表名/别名。
     */
    String tableAlias() default "";
}
