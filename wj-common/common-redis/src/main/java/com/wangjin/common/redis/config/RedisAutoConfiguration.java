package com.wangjin.common.redis.config;

import com.wangjin.common.redis.service.RedisService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * common-redis 自动装配。
 */
@AutoConfiguration
@ConditionalOnClass(RedisTemplate.class)
@Import({RedisConfig.class, RedisCacheConfig.class, RedisService.class})
public class RedisAutoConfiguration {
}
