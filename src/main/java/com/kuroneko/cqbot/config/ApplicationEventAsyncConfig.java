package com.kuroneko.cqbot.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ApplicationEventAsyncConfig {

    private final ThreadPoolTaskExecutor listenerExecutor;

    public ApplicationEventAsyncConfig(@Qualifier("listenerExecutor") ThreadPoolTaskExecutor listenerExecutor) {
        this.listenerExecutor = listenerExecutor;
    }

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        //创建一个事件广播器
        SimpleApplicationEventMulticaster result = new SimpleApplicationEventMulticaster();
        //设置异步执行器,来完成异步执行监听事件这样会导致所有的监听器都异步执行
        result.setTaskExecutor(listenerExecutor);
        return result;
    }
}
