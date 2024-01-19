package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.exception.BotException;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class CacheUtil {
    public static final ExpiringMap<String, Collection<String>> expiringMap = ExpiringMap.builder()
            //允许更新过期时间值,如果不设置variableExpiration，不允许后面更改过期时间,一旦执行更改过期时间操作会抛异常UnsupportedOperationException
            .variableExpiration()
//            1）ExpirationPolicy.ACCESSED ：每进行一次访问，过期时间就会重新计算；
//            2）ExpirationPolicy.CREATED：在过期时间内重新 put 值的话，过期时间重新计算；
            .expirationPolicy(ExpirationPolicy.CREATED)
            //设置每个key有效时间30m,如果key不设置过期时间，key永久有效
//            .expiration(30L, TimeUnit.MINUTES)
            .build();

    public static Collection<String> put(String key, String value) {
        List<String> list = Collections.singletonList(value);
        return put(key, list);
    }

    public static Collection<String> put(String key, Collection<String> list) {
        return expiringMap.put(key, list);
    }

    public static Collection<String> put(String key, String value, long duration, TimeUnit timeUnit) {
        List<String> list = Collections.singletonList(value);
        return put(key, list, duration, timeUnit);
    }

    public static Collection<String> put(String key, Collection<String> list, long duration, TimeUnit timeUnit) {
        return expiringMap.put(key, list, duration, timeUnit);
    }

    public static Collection<String> remove(String key) {
        return expiringMap.remove(key);
    }

    public static Collection<String> get(String key) {
        return expiringMap.get(key);
    }

    public static Collection<String> getOrPut(String key, long duration, TimeUnit timeUnit, Callable<Collection<String>> callable) {
        Collection<String> cacheMsg = CacheUtil.get(key);
        if (cacheMsg != null) {
            return cacheMsg;
        }
        Collection<String> call;
        try {
            call = callable.call();
        } catch (Exception e) {
            if (e instanceof BotException be) {
                throw be;
            } else {
                throw new RuntimeException(e);
            }
        }
        CacheUtil.put(key, call, duration, timeUnit);
        return call;
    }
}
