package com.ailong.queue;

import static com.ailong.queue.util.UnsafeLongArrayAccess.allocateLongArray;
import static com.ailong.queue.util.UnsafeLongArrayAccess.calcCircularLongElementOffset;
import static com.ailong.queue.util.UnsafeLongArrayAccess.soLongElement;

/**
 * Created by jiangfei on 2020/5/10.
 */
public abstract class ConcurrentSequencedCircularArrayQueue<E> extends ConcurrentCircularArrayQueue<E> {

    //多生产者==多消费者 序列数组
    protected final long[] sequenceBuffer;

    public ConcurrentSequencedCircularArrayQueue(int capacity) {
        super(capacity);
        int actualCapacity = (int) (this.mask + 1);
        // pad data on either end with some empty slots. Note that actualCapacity is <= MAX_POW2_INT
        sequenceBuffer = allocateLongArray(actualCapacity);
        for (long i = 0; i < actualCapacity; i++) {
            //0，1，2，3，4，5.....length
            soLongElement(sequenceBuffer, calcCircularLongElementOffset(i, mask), i);
        }
    }

}
