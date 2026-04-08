package com.kuroneko.cqbot;

import com.kuroneko.cqbot.service.aiTool.AiWebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class CqBotApplicationTests {

    @Test
    void contextLoads() {
        AiWebSearchTool aiWebSearchTool = new AiWebSearchTool(10, 10000);
        String springBoot = aiWebSearchTool.webSearch("游戏 原神 版本 最新", 10);
        System.out.println(springBoot);
    }

}
