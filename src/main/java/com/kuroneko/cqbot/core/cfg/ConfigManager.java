package com.kuroneko.cqbot.core.cfg;

import com.kuroneko.cqbot.config.localCfg.AdminCfg;
import com.kuroneko.cqbot.config.localCfg.BiliCfg;
import com.kuroneko.cqbot.config.localCfg.UpdateCfg;
import com.kuroneko.cqbot.utils.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Slf4j
@Getter
public class ConfigManager {
    public static final ConfigManager ins = new ConfigManager();
    public static final String CONFIG_PATH = "configs/";
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private AdminCfg adminCfg;
    private BiliCfg biliCfg;
    private UpdateCfg updateCfg;

    private ConfigManager() {
        loadAll();
    }

    public void loadAll() {
        lock.writeLock().lock();
        try {
            Class<? extends ConfigManager> clazz = this.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field field : declaredFields) {
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();

                if (ConfigSave.class.isAssignableFrom(fieldType)) {
                    try {
                        field.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        ConfigSave config = load((Class<? extends ConfigSave>) fieldType);
                        field.set(this, config);
                        log.debug("Successfully loaded configuration: {}", fieldName);
                    } catch (Exception e) {
                        log.error("Failed to load configuration for field: {}", fieldName, e);
                        throw new RuntimeException("Failed to load configuration for field: " + fieldName, e);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T extends ConfigSave> T load(Class<T> configClass) throws Exception {
        String fileName = CONFIG_PATH + configClass.getSimpleName() + ".json";
        Path path = Path.of(fileName);

        if (Files.exists(path)) {
            try {
                String jsonString = Files.readString(path);
                return JsonUtil.toBean(jsonString, configClass);
            } catch (IOException e) {
                log.error("Failed to read configuration file: {}", fileName, e);
                throw new RuntimeException("Failed to read configuration file: " + fileName, e);
            }
        } else {
            log.info("Configuration file not found, use default: {}", fileName);

            Constructor<T> constructor = configClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        }
    }

    /**
     * 重新加载指定类型的配置
     */
    public <T extends ConfigSave> T reloadConfig(Class<T> configClass) {
        lock.writeLock().lock();
        try {
            Class<? extends ConfigManager> clazz = this.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();

            for (Field field : declaredFields) {
                if (field.getType().equals(configClass)) {
                    field.setAccessible(true);
                    T config = load(configClass);
                    field.set(this, config);
                    log.info("Successfully reloaded configuration: {}", configClass.getSimpleName());
                    return config;
                }
            }
            throw new IllegalArgumentException("Configuration type not found in ConfigManager: " + configClass.getName());
        } catch (Exception e) {
            log.error("Failed to reload configuration: {}", configClass.getSimpleName(), e);
            throw new RuntimeException("Failed to reload configuration: " + configClass.getSimpleName(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}