package com.ailong.disruptor;

/**
 * 数据生产者
 * Created by jiangfei on 2020/5/8.
 */
public interface DataProvider<T> {

    T get(long sequence);
}
