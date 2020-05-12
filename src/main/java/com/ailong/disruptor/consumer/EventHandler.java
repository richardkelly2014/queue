package com.ailong.disruptor.consumer;

/**
 * 事件处理 句柄
 * Created by jiangfei on 2020/5/8.
 */
public interface EventHandler<T> {

    /**
     * 处理类
     *
     * @param event
     * @param sequence
     * @param endOfBatch
     * @throws Exception
     */
    void onEvent(T event, long sequence, boolean endOfBatch) throws Exception;
}
