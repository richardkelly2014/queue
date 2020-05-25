package com.ailong.test.executor;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * Created by jiangfei on 2020/5/24.
 */
@Slf4j
public class PTask<V> implements RunnableFuture<V> {


    public PTask(Runnable runnable, V value) {

    }

    public PTask(Callable<V> callable) {

    }

    @Override
    public void run() {
        log.info("{}", 111);

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
