package com.kuroneko.cqbot.core.process;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class CommonProcessor extends BaseMessageProcess {
    private static final String THREAD_NAME = "common";
    private static final CommonProcessor ins = new CommonProcessor(THREAD_NAME);

    public static CommonProcessor getInstance() {
        return ins;
    }

    public CommonProcessor(String threadName) {
        super(threadName);
    }

    @Override
    public <T> Future<T> submit(Callable<T> message) {
        return executor.submit(message);
    }

    @Override
    public void execute(Runnable message) {
        executor.execute(message);
    }
}
