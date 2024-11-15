package com.kuroneko.cqbot.core.cfg;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Slf4j
@Configuration
public class BotAsyncTask {

    @Bean("botTaskExecutor")
    public ExecutorService botTaskExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

}
