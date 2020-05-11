package com.ailong.disruptor;

/**
 * 序列屏障
 */
public interface SequenceBarrier {

    /**
     * 等待
     *
     * @param sequence
     * @return
     * @throws AlertException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    long waitFor(long sequence) throws AlertException, InterruptedException, TimeoutException;

    long getCursor();

    boolean isAlerted();

    void alert();

    void clearAlert();

    void checkAlert() throws AlertException;
}
