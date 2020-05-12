package com.ailong.disruptor.consumer;

import com.ailong.disruptor.Sequence;

/**
 * 事件处理者
 */
public interface EventProcessor extends Runnable {

    Sequence getSequence();

    /**
     * 停止
     */
    void halt();

    boolean isRunning();
}
