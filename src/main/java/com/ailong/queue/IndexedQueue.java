package com.ailong.queue;

public interface IndexedQueue {

    /**
     * 消费者索引
     *
     * @return
     */
    long lvConsumerIndex();

    /**
     * 生产者索引
     *
     * @return
     */
    long lvProducerIndex();

}
