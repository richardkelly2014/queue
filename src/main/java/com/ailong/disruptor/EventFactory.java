package com.ailong.disruptor;

/**
 * 事件创建工厂
 * Created by jiangfei on 2020/5/8.
 */
public interface EventFactory<T> {

    T newInstance();
}
