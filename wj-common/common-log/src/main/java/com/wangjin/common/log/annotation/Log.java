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

    /** 模块标题 */
    String title() default "";

    BusinessType businessType() default BusinessType.OTHER;

    OperatorType operatorType() default OperatorType.MANAGE;

    boolean isSaveRequestData() default true;

    boolean isSaveResponseData() default true;

    String[] excludeParamNames() default {};
}
