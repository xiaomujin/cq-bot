package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.exception.BotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class TaskService {
    @Qualifier("listenerExecutor")
    private final ThreadPoolTaskExecutor listenerExecutor;


    public void execute(Runnable task) {
        listenerExecutor.execute(task);
    }

    public Future<?> submitTask(Runnable task) {
        return listenerExecutor.submit(task);
    }

    public <T> Future<T> submitTask(Callable<T> task) {
        return listenerExecutor.submit(task);
    }

    public void submitTask(Runnable task, int timeout) {
        Future<?> future = submitTask(task);
        try {
            future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BotException("电波无法到达了~");
        }
    }

    public <T> T submitTask(Callable<T> task, int timeout) {
        Future<T> future = submitTask(task);
        try {
            return future.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new BotException("电波无法到达了~");
        }
    }

}
