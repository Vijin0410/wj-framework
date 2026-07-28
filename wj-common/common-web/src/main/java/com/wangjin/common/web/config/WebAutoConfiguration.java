package com.wangjin.common.web.config;

import com.wangjin.common.web.aspect.DictAspect;
import com.wangjin.common.web.aspect.DuplicateSubmitAspect;
import com.wangjin.common.web.exception.GlobalExceptionHandler;
import com.wangjin.common.web.util.DictUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    /**
     * 防重提交依赖 Redisson，无 Redis 时不装配。
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "org.redisson.api.RedissonClient")
    @Import(DuplicateSubmitAspect.class)
    static class DuplicateSubmitConfiguration {
    }

    /**
     * 字典翻译依赖 Redis。
     */
    @AutoConfiguration
    @ConditionalOnClass(name = "org.springframework.data.redis.core.RedisTemplate")
    @Import({DictUtils.class, DictAspect.class})
    static class DictConfiguration {
    }
}
