package com.kuroneko.cqbot.core.process;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseMessageProcess implements IMessageProcessor {

    protected ExecutorService executor;

    protected String threadName;

    public BaseMessageProcess(String threadName) {
        this.threadName = threadName;
        init();
    }

    @Override
    public void init() {
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, threadName));
    }

    @Override
    public ExecutorService getExecutor() {
        return this.executor;
    }

}
