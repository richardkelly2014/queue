package com.ailong.disruptor;

/**
 * 序列
 */
public interface Sequenced {

    /**
     * 数据结构 缓存大小
     *
     * @return
     */
    int getBufferSize();

    boolean hasAvailableCapacity(int requiredCapacity);

    /**
     * 剩余容量
     *
     * @return
     */
    long remainingCapacity();

    /**
     * 下一个序列
     *
     * @return
     */
    long next();

    long next(int n);

    /**
     * try next
     *
     * @return
     * @throws InsufficientCapacityException
     */
    long tryNext() throws InsufficientCapacityException;

    long tryNext(int n) throws InsufficientCapacityException;

    /**
     * 发布 序列
     *
     * @param sequence
     */
    void publish(long sequence);

    void publish(long lo, long hi);
}
