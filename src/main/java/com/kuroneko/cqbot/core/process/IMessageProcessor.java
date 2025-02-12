package com.kuroneko.cqbot.core.process;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface IMessageProcessor {
    <T> Future<T> submit(Callable<T> message);

    <T> T submitSync(Callable<T> message);

    void execute(Runnable message);

    ExecutorService getExecutor();

    void init();
}
