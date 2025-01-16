package com.kuroneko.cqbot.core.cfg;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public interface ConfigSave {
    ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

//    default ConfigSave load() {
//        lock.readLock().lock();
//        ConfigSave configSave;
//        try {
//            String cfgName = this.getClass().getSimpleName() + ".json";
//            Path path = Path.of(cfgName);
//            if (Files.exists(path)) {
//                String jsonString = Files.readString(path);
//                configSave = JSON.parseObject(jsonString, this.getClass());
//            } else {
//                configSave = this.getClass().getDeclaredConstructor().newInstance();
//            }
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.readLock().unlock();
//        }
//        return configSave;
//    }

    default void save() {
        lock.writeLock().lock();
        try {
            String cfgName = ConfigManager.cfgPath + this.getClass().getSimpleName() + ".json";
            String jsonString = JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.FieldBased);
            Path path = Path.of(cfgName);
            path.getParent().toFile().mkdirs();
            Files.writeString(path, jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
