package com.ailong.disruptor.consumer;

import com.ailong.disruptor.Sequence;

import java.util.*;

/**
 * 消费者 集合
 */
public class ConsumerRepository<T> implements Iterable<ConsumerInfo> {

    //事件处理器--->事件处理者信息(消费)
    private final Map<EventHandler<?>, EventProcessorInfo<T>> eventProcessorInfoByEventHandler =
            new IdentityHashMap<>();

    //序列---->消费
    private final Map<Sequence, ConsumerInfo> eventProcessorInfoBySequence =
            new IdentityHashMap<>();

    //消费者集合
    private final Collection<ConsumerInfo> consumerInfos = new ArrayList<>();

    @Override
    public Iterator<ConsumerInfo> iterator() {
        return null;
    }
}
