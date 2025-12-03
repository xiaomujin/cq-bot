package com.kuroneko.cqbot.core.cfg;

import com.kuroneko.cqbot.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 配置保存基类，提供配置持久化功能
 * 所有需要持久化的配置类都应继承此类
 */
@Slf4j
public abstract class ConfigSave implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // 每个配置实例使用独立的锁，避免全局锁竞争
    private final ReentrantLock saveLock = new ReentrantLock();

    /**
     * 保存当前配置对象到JSON文件
     * 文件命名规则：CONFIG_PATH/ClassName.json
     *
     * @throws RuntimeException 当保存失败时抛出
     */
    public void save() {
        saveLock.lock();
        try {
            String className = this.getClass().getSimpleName();
            String filePath = ConfigManager.CONFIG_PATH + className + ".json";
            Path path = Path.of(filePath);
            log.debug("Saving configuration to: {}", filePath);

            Files.createDirectories(path.getParent());

            // 序列化为JSON字符串
            String jsonString = JsonUtil.toPrettyString(this);

            // 写入文件，指定字符集和写入选项
            Files.writeString(
                    path,
                    jsonString,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            log.debug("Successfully saved configuration: {}", className);
        } catch (IOException e) {
            String errorMsg = String.format("Failed to save configuration: %s", this.getClass().getSimpleName());
            log.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } finally {
            saveLock.unlock();
        }
    }

}