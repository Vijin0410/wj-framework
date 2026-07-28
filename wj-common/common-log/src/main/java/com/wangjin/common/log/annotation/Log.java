package com.wangjin.common.log.annotation;

import com.wangjin.common.log.enums.BusinessType;
import com.wangjin.common.log.enums.OperatorType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /** 操作标题 */
    String title() default "";

    /** 业务操作类型 */
    BusinessType businessType() default BusinessType.OTHER;

    /** 操作人类别 */
    OperatorType operatorType() default OperatorType.MANAGE;

    /** 是否保存请求参数 */
    boolean isSaveRequestData() default true;

    /** 是否保存响应结果 */
    boolean isSaveResponseData() default true;

    /** 额外排除的参数字段名 */
    String[] excludeParamNames() default {};

    /** 业务主键 SpEL，如 #id */
    String bizNo() default "";
}
