package com.ailong.queue;

/**
 * 队列处理 游标
 */
public interface QueueProgressIndicators {

    /**
     * 当前 生产者 索引
     *
     * @return
     */
    long currentProducerIndex();

    /**
     * 当前消费者 索引
     *
     * @return
     */
    long currentConsumerIndex();
}
