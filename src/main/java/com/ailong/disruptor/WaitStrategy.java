package com.ailong.disruptor;

/**
 * 等待策略
 */
public interface WaitStrategy {

    /**
     * 等待
     *
     * @param sequence
     * @param cursor
     * @param dependentSequence
     * @param barrier
     * @return
     * @throws AlertException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    long waitFor(long sequence, Sequence cursor, Sequence dependentSequence, SequenceBarrier barrier)
            throws AlertException, InterruptedException, TimeoutException;

    /**
     * 发信号
     */
    void signalAllWhenBlocking();
}
