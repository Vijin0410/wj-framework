package com.wangjin.common.log.config;

import com.wangjin.common.log.aspect.LogAspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

/**
 * common-log 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.aspectj.lang.annotation.Aspect")
@Import(LogAspect.class)
public class LogAutoConfiguration {
}
