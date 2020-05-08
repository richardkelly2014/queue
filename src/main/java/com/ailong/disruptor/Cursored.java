package com.ailong.disruptor;

/**
 * 操作游标
 * Created by jiangfei on 2020/5/8.
 */
public interface Cursored {
    /**
     * 当前游标位置
     *
     * @return
     */
    long getCursor();
}
