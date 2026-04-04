package com.kuroneko.cqbot.service.aiTool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class AiHelpTool {

    @Tool(name = "help", description = "获取ai的帮助信息。适用于询问ai的功能、指令、帮助列表。")
    public List<String> help() {
        log.info("help 执行");
        return List.of("我的本周词云", "任务流程图", "任务物品图", "日报", "日历");
    }
}
