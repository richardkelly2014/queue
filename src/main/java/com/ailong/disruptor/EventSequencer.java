package com.ailong.disruptor;

/**
 * 事件序列
 *
 * @param <T>
 */
public interface EventSequencer<T> extends DataProvider<T>, Sequenced {

}
