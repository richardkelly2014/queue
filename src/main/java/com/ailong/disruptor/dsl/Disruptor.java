package com.ailong.disruptor.dsl;

import com.ailong.disruptor.EventFactory;
import com.ailong.disruptor.ExceptionHandler;
import com.ailong.disruptor.RingBuffer;
import com.ailong.disruptor.WaitStrategy;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

public class Disruptor<T> {

    private final RingBuffer<T> ringBuffer;
    private final Executor executor;

    private final AtomicBoolean started = new AtomicBoolean(false);
    private ExceptionHandler<? super T> exceptionHandler = new ExceptionHandlerWrapper<>();

    public Disruptor(
            final EventFactory<T> eventFactory,
            final int ringBufferSize,
            final ThreadFactory threadFactory,
            final ProducerType producerType,
            final WaitStrategy waitStrategy) {
        this(
                RingBuffer.create(producerType, eventFactory, ringBufferSize, waitStrategy),
                new BasicExecutor(threadFactory));
    }

    private Disruptor(final RingBuffer<T> ringBuffer, final Executor executor) {
        this.ringBuffer = ringBuffer;
        this.executor = executor;
    }


}

