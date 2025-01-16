package com.kuroneko.cqbot.core.cfg;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.JSONWriter;
import com.kuroneko.cqbot.config.localCfg.AdminCfg;
import com.kuroneko.cqbot.config.localCfg.BiliCfg;
import com.kuroneko.cqbot.config.localCfg.UpdateCfg;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


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
        try {
            Class<? extends ConfigManager> clazz = this.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                Class<?> fieldType = declaredField.getType();
                Class<?>[] interfaces = fieldType.getInterfaces();
                boolean match = Arrays.asList(interfaces).contains(ConfigSave.class);
                if (match) {
                    declaredField.setAccessible(true);
                    Object load = load(fieldType);
//                    Object instance = fieldType.getDeclaredConstructor().newInstance();
//                    Method load1 = ConfigSave.class.getMethod("load");
//                    Object invoke = load1.invoke(instance);
                    declaredField.set(this, load);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private <T> T load(Class<T> clazz) {
        T configSave;
        try {
            String cfgName = cfgPath + clazz.getSimpleName() + ".json";
            Path path = Path.of(cfgName);
            if (Files.exists(path)) {
                String jsonString = Files.readString(path);
                configSave = JSON.parseObject(jsonString, clazz, JSONReader.Feature.FieldBased);
            } else {
                configSave = clazz.getDeclaredConstructor().newInstance();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return configSave;
    }
}
