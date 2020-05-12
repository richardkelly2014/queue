package com.ailong.disruptor.consumer;

import com.ailong.disruptor.Sequence;
import com.ailong.disruptor.SequenceBarrier;

import java.util.concurrent.Executor;

/**
 * 事件处理者 信息
 *
 * @param <T>
 */
public class EventProcessorInfo<T> implements ConsumerInfo {

    //事件处理者
    private final EventProcessor eventprocessor;
    //事件处理器
    private final EventHandler<? super T> handler;
    //序列围栏
    private final SequenceBarrier barrier;
    private boolean endOfChain = true;


    EventProcessorInfo(
            final EventProcessor eventprocessor, final EventHandler<? super T> handler, final SequenceBarrier barrier) {
        this.eventprocessor = eventprocessor;
        this.handler = handler;
        this.barrier = barrier;
    }


    public EventProcessor getEventProcessor() {
        return eventprocessor;
    }

    @Override
    public Sequence[] getSequences() {
        return new Sequence[]{eventprocessor.getSequence()};
    }

    public EventHandler<? super T> getHandler() {
        return handler;
    }

    @Override
    public SequenceBarrier getBarrier() {
        return barrier;
    }

    @Override
    public boolean isEndOfChain() {
        return endOfChain;
    }

    @Override
    public void start(final Executor executor) {
        executor.execute(eventprocessor);
    }

    @Override
    public void halt() {
        eventprocessor.halt();
    }

    /**
     *
     */
    @Override
    public void markAsUsedInBarrier() {
        endOfChain = false;
    }

    @Override
    public boolean isRunning() {
        return eventprocessor.isRunning();
    }
}
