package com.kuroneko.cqbot.core.cfg;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigSave implements Serializable {
    public void save() {
        try {
            String cfgName = ConfigManager.cfgPath + this.getClass().getSimpleName() + ".json";
            String jsonString = JSON.toJSONString(this, JSONWriter.Feature.PrettyFormat, JSONWriter.Feature.FieldBased);
            Path path = Path.of(cfgName);
            path.getParent().toFile().mkdirs();
            Files.writeString(path, jsonString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
