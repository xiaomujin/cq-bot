package com.kuroneko.cqbot.service.aiTool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@Slf4j
public class AiTimeTool {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private static final DateTimeFormatter ISO_OFFSET_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Tool(name = "current_time", description = "获取当前系统时间。适用于询问现在几点、今天几号、当前日期时间、时区等场景。")
    public String currentTime() {
        log.info("current_time 执行");
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        return "当前系统时间：" + DATE_TIME_FORMATTER.format(now)
                + "\n时区：" + zoneId
                + "\nISO时间：" + ISO_OFFSET_FORMATTER.format(now)
                + "\nUnix时间戳（秒）：" + now.toEpochSecond()
                + "\nUnix时间戳（毫秒）：" + Instant.from(now).toEpochMilli();
    }
}
