package com.ailong.disruptor.consumer;

import com.ailong.disruptor.Sequence;
import com.ailong.disruptor.SequenceBarrier;

import java.util.concurrent.Executor;

public interface ConsumerInfo {

    Sequence[] getSequences();

    SequenceBarrier getBarrier();

    boolean isEndOfChain();

    void start(Executor executor);

    void halt();

    void markAsUsedInBarrier();

    boolean isRunning();
}
