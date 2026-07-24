package com.wangjin.common.web.config;

import com.wangjin.common.web.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * common-web 自动装配入口。
 */
@AutoConfiguration
@Import({
        GlobalExceptionHandler.class,
        JacksonConfig.class,
        ValidationConfig.class,
        CorsConfig.class,
        RestTemplateConfig.class
})
public class WebAutoConfiguration {
}
