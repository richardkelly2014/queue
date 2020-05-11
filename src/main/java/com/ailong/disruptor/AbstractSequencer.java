package com.ailong.disruptor;

import com.ailong.disruptor.util.Util;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * 序列器 -- 抽象
 */
public abstract class AbstractSequencer implements Sequencer {

    private static final AtomicReferenceFieldUpdater<AbstractSequencer, Sequence[]> SEQUENCE_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(AbstractSequencer.class, Sequence[].class, "gatingSequences");

    protected final int bufferSize;
    protected final WaitStrategy waitStrategy;

    //游标
    protected final Sequence cursor = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);
    //游标数组？
    protected volatile Sequence[] gatingSequences = new Sequence[0];

    public AbstractSequencer(int bufferSize, WaitStrategy waitStrategy) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }

        this.bufferSize = bufferSize;
        this.waitStrategy = waitStrategy;
    }

    /**
     * @see Sequencer#getCursor()
     */
    @Override
    public final long getCursor() {
        return cursor.get();
    }

    /**
     * @see Sequencer#getBufferSize()
     */
    @Override
    public final int getBufferSize() {
        return bufferSize;
    }

    @Override
    public final void addGatingSequences(Sequence... gatingSequences) {
        SequenceGroups.addSequences(this, SEQUENCE_UPDATER, this, gatingSequences);
    }

    /**
     * @see Sequencer#removeGatingSequence(Sequence)
     */
    @Override
    public boolean removeGatingSequence(Sequence sequence) {
        return SequenceGroups.removeSequence(this, SEQUENCE_UPDATER, sequence);
    }

    @Override
    public long getMinimumSequence() {
        return Util.getMinimumSequence(gatingSequences, cursor.get());
    }


    @Override
    public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
        return new ProcessingSequenceBarrier(this, waitStrategy, cursor, sequencesToTrack);
    }

    @Override
    public <T> EventPoller<T> newPoller(DataProvider<T> dataProvider, Sequence... gatingSequences) {
        return EventPoller.newInstance(dataProvider, this, new Sequence(), cursor, gatingSequences);
    }

    @Override
    public String toString() {
        return "AbstractSequencer{" +
                "waitStrategy=" + waitStrategy +
                ", cursor=" + cursor +
                ", gatingSequences=" + Arrays.toString(gatingSequences) +
                '}';
    }

}
