package com.wangjin.common.redis.config;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Spring Cache + Redis 配置。
 */
@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory, CacheProperties cacheProperties) {
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(factory))
                .cacheDefaults(redisCacheConfiguration(cacheProperties))
                .build();
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(CacheProperties cacheProperties) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()))
                .computePrefixWith(name -> name + ":");

        CacheProperties.Redis redis = cacheProperties.getRedis();
        if (redis.getTimeToLive() != null) {
            config = config.entryTtl(redis.getTimeToLive());
        }
        if (!redis.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }
        if (!redis.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }
        return config;
    }
}
