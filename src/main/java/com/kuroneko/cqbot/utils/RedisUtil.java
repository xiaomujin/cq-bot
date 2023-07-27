package com.kuroneko.cqbot.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 所有缓存Key的前缀
     */
    private static final String CACHE_KEY_PREFIX = "Cache:";

    public Long add(String key, Object value) {
        return redisTemplate.boundSetOps(CACHE_KEY_PREFIX + key).add(value);
    }

    public Long remove(String key, Object value) {
        return redisTemplate.boundSetOps(CACHE_KEY_PREFIX + key).remove(value);
    }

    public void put(String key, String hKey, Object hValue) {
        redisTemplate.boundHashOps(CACHE_KEY_PREFIX + key).put(hKey, hValue);
    }

    public void set(String key, Object value) {
        redisTemplate.boundValueOps(CACHE_KEY_PREFIX + key).set(value);
    }

    public void set(String key, Object value, long timeoutSeconds) {
        redisTemplate.boundValueOps(CACHE_KEY_PREFIX + key).set(value, timeoutSeconds, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redisTemplate.boundValueOps(CACHE_KEY_PREFIX + key).get();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, String hKey) {
        return (T) redisTemplate.boundHashOps(CACHE_KEY_PREFIX + key).get(hKey);
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> members(String key) {
        return (Set<T>) redisTemplate.boundSetOps(CACHE_KEY_PREFIX + key).members();
    }

    public void delete(String... key) {
        List<String> withPrefixKeys = Stream.of(key).map(i -> CACHE_KEY_PREFIX + i).collect(Collectors.toList());
        redisTemplate.delete(withPrefixKeys);
    }

    public Collection<String> getAllKeys(String subKey) {
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + subKey + ":*");
        if (keys != null) {
            // 去掉缓存key的common prefix前缀
            return keys.stream().map(key -> StrUtil.removePrefix(key, CACHE_KEY_PREFIX + subKey + ":")).collect(Collectors.toSet());
        } else {
            return CollUtil.newHashSet();
        }
    }

//    public Collection<Object> getAllValues() {
//        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
//        if (keys != null) {
//            return redisTemplate.opsForValue().multiGet(keys);
//        } else {
//            return CollUtil.newArrayList();
//        }
//    }

//    public Map<String, Object> getAllKeyValues(String subKey) {
//        Collection<String> allKeys = this.getAllKeys(subKey);
//        HashMap<String, Object> results = MapUtil.newHashMap();
//        for (String key : allKeys) {
//            results.put(key, this.get(key));
//        }
//        return results;
//    }

}
