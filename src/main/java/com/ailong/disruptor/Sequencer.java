package com.ailong.disruptor;

/**
 * 序列器
 */
public interface Sequencer extends Cursored, Sequenced {

    // 序列起始位置
    long INITIAL_CURSOR_VALUE = -1L;

    void claim(long sequence);

    /**
     * 是否可用
     *
     * @param sequence
     * @return
     */
    boolean isAvailable(long sequence);

    void addGatingSequences(Sequence... gatingSequences);

    boolean removeGatingSequence(Sequence sequence);

    /**
     * 新建 序列屏障
     *
     * @param sequencesToTrack
     * @return
     */
    SequenceBarrier newBarrier(Sequence... sequencesToTrack);

    long getMinimumSequence();

    long getHighestPublishedSequence(long nextSequence, long availableSequence);

    /**
     * 新建事件消费者
     *
     * @param provider
     * @param gatingSequences
     * @param <T>
     * @return
     */
    <T> EventPoller<T> newPoller(DataProvider<T> provider, Sequence... gatingSequences);
}
