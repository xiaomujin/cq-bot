package com.kuroneko.cqbot.core.cfg;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.kuroneko.cqbot.config.localCfg.AdminCfg;
import com.kuroneko.cqbot.config.localCfg.BiliCfg;
import com.kuroneko.cqbot.config.localCfg.UpdateCfg;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;


@Slf4j
@Getter
public class ConfigManager {
    public static final ConfigManager ins = new ConfigManager();
    public static final String cfgPath = "configs/";
    private AdminCfg adminCfg;
    private BiliCfg biliCfg;
    private UpdateCfg updateCfg;

    private ConfigManager() {
        loadAll();
    }

    public void loadAll() {
        String declaredFieldName = "";
        try {
            Class<? extends ConfigManager> clazz = this.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredFieldName = declaredField.getName();
                Class<?> fieldType = declaredField.getType();
                if (ConfigSave.class.isAssignableFrom(fieldType)) {
                    declaredField.setAccessible(true);
                    Object load = load(fieldType);
                    declaredField.set(this, load);
                }
            }
        } catch (Exception e) {
            log.error("Failed to load configuration for field: {}", declaredFieldName, e);
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private <T> T load(Class<T> clazz) throws Exception {
        T configSave;
        String cfgName = cfgPath + clazz.getSimpleName() + ".json";
        Path path = Path.of(cfgName);
        if (Files.exists(path)) {
            String jsonString = Files.readString(path);
            configSave = JSON.parseObject(jsonString, clazz, JSONReader.Feature.FieldBased);
        } else {
            configSave = clazz.getDeclaredConstructor().newInstance();
        }
        return configSave;
    }
}
