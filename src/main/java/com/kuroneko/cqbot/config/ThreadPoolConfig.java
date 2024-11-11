package com.kuroneko.cqbot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;

@Slf4j
@Configuration
public class ThreadPoolConfig {
//    public static final int cpuNum = Runtime.getRuntime().availableProcessors();

    @Bean("listenerExecutor")
    public ThreadPoolTaskExecutor listenerExecutor() {
        ThreadPoolTaskExecutor listenerExecutor = new ThreadPoolTaskExecutor();
        //设置线程池参数信息
        listenerExecutor.setCorePoolSize(10);
        listenerExecutor.setMaxPoolSize(40);
        listenerExecutor.setQueueCapacity(200);
        listenerExecutor.setKeepAliveSeconds(60);
        listenerExecutor.setThreadNamePrefix("listener-");
        listenerExecutor.setWaitForTasksToCompleteOnShutdown(true);
        // 设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
        listenerExecutor.setAwaitTerminationSeconds(60);
        //修改拒绝策略为使用当前线程执行
        listenerExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        //初始化线程池
        listenerExecutor.initialize();
        return listenerExecutor;
    }

}
