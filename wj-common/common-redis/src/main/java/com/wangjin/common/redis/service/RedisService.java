package com.wangjin.common.redis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 常用操作封装。
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "rawtypes"})
public class RedisService {

    private final RedisTemplate redisTemplate;

    public <T> void setCacheObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    public boolean expire(String key, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.expire(key, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire == null ? -1 : expire;
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> ops = redisTemplate.opsForValue();
        return ops.get(key);
    }

    public boolean deleteObject(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean deleteObject(Collection keys) {
        Long count = redisTemplate.delete(keys);
        return count != null && count > 0;
    }

    public void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public <T> long setCacheList(String key, List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    public <T> List<T> getCacheList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    public <T> BoundSetOperations<String, T> setCacheSet(String key, Set<T> dataSet) {
        BoundSetOperations<String, T> ops = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext()) {
            ops.add(it.next());
        }
        return ops;
    }

    public <T> Set<T> getCacheSet(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    public <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    public <T> Map<String, T> getCacheMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    public <T> void setCacheMapValue(String key, String hKey, T value) {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    public <T> T getCacheMapValue(String key, String hKey) {
        HashOperations<String, String, T> ops = redisTemplate.opsForHash();
        return ops.get(key, hKey);
    }

    public boolean deleteCacheMapValue(String key, String hKey) {
        return redisTemplate.opsForHash().delete(key, hKey) > 0;
    }

    public Collection<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
}
