package com.kuroneko.cqbot.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JsonUtil {
    public static JsonMapper mapper;

    private JsonUtil(JsonMapper mapper) {
        JsonUtil.mapper = mapper;
    }

    /**
     * 对象转换为json字符串
     *
     * @param obj 需要转换的对象
     * @return json
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JacksonException e) {
            log.error("json解析出错：{}", obj, e);
            throw new RuntimeException(e);
        }
    }


    public static String toPrettyString(Object obj) {
        if (obj == null) {
            return "";
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JacksonException e) {
            log.error("json解析出错：{}", obj, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T toBean(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (JacksonException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static JsonNode toNode(String json) {
        try {
            return mapper.readTree(json);
        } catch (JacksonException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static <E> List<E> toList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (JacksonException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static <K, V> Map<K, V> toMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (JacksonException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (JacksonException e) {
            log.error("json解析出错：{}", json, e);
            throw new RuntimeException(e);
        }
    }

}