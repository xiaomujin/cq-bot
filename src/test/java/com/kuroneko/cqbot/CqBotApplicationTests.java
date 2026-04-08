package com.kuroneko.cqbot;

import com.kuroneko.cqbot.service.aiTool.AiWebSearchTool;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class CqBotApplicationTests {

    @Test
    void contextLoads() {
        AiWebSearchTool aiWebSearchTool = new AiWebSearchTool(10, 10000, "https://www.bing.com/search");
        String springBoot = aiWebSearchTool.webSearch("终末地 莱万汀 爱好", 5);
        System.out.println(springBoot);
    }

}
