package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {
    public final static ObjectMapper objectMapper = ApplicationContextHandler.getBean(ObjectMapper.class);

    private JsonUtil() {
    }

    /**
     * 对象转换为json字符串
     *
     * @param obj 需要转换的对象
     * @return json
     */
    @Nullable
    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("json解析出错：{}", obj, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return objectMapper.readValue(json, tClass);
        } catch (IOException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

}