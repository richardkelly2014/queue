package com.ailong.queue;

import com.ailong.queue.util.Pow2;

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.ailong.queue.util.UnsafeRefArrayAccess.allocateRefArray;
import static com.ailong.queue.util.UnsafeRefArrayAccess.calcCircularRefElementOffset;
import static com.ailong.queue.util.UnsafeRefArrayAccess.lvRefElement;

/**
 * 圆形 数组操作 队列
 * <p>
 * 数组长度必须是2的N次方，mask = 2的N次方-1
 * </p>
 *
 * @param <E>
 */
public abstract class ConcurrentCircularArrayQueue<E> extends ConcurrentCircularArrayQueueL0Pad<E>
        implements MessagePassingQueue<E>, IndexedQueue, QueueProgressIndicators, SupportsIterator {

    // mask = length-1
    protected final long mask;
    // 数组
    protected final E[] buffer;

    ConcurrentCircularArrayQueue(int capacity) {
        // 2的n 次方
        int actualCapacity = Pow2.roundToPowerOfTwo(capacity);
        // size - 1
        mask = actualCapacity - 1;
        // 数组
        buffer = allocateRefArray(actualCapacity);
    }

    @Override
    public int size() {
        return IndexedQueueSizeUtil.size(this);
    }

    @Override
    public boolean isEmpty() {
        return IndexedQueueSizeUtil.isEmpty(this);
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public void clear() {
        while (poll() != null) {
            // if you stare into the void
        }
    }

    @Override
    public int capacity() {
        return (int) (mask + 1);
    }

    @Override
    public long currentProducerIndex() {
        return lvProducerIndex();
    }

    @Override
    public long currentConsumerIndex() {
        return lvConsumerIndex();
    }

    @Override
    public Iterator<E> iterator() {
        final long cIndex = lvConsumerIndex();
        final long pIndex = lvProducerIndex();

        return new WeakIterator(cIndex, pIndex, mask, buffer);
    }

    /**
     * 使用的瞬间 生产索引和消费索引 固定
     * 不建议这么使用
     *
     * @param <E>
     */
    private static class WeakIterator<E> implements Iterator<E> {

        private final long pIndex;
        private final long mask;
        private final E[] buffer;
        private long nextIndex;
        private E nextElement;

        WeakIterator(long cIndex, long pIndex, long mask, E[] buffer) {
            this.nextIndex = cIndex;
            this.pIndex = pIndex;
            this.mask = mask;
            this.buffer = buffer;
            nextElement = getNext();
        }

        /**
         * 不允许删除
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public boolean hasNext() {

            return nextElement != null;
        }

        @Override
        public E next() {
            final E e = nextElement;
            if (e == null)
                throw new NoSuchElementException();
            // 取一下个
            nextElement = getNext();
            // 返回当前
            return e;
        }

        /**
         * 下一个元素
         *
         * @return
         */
        private E getNext() {
            while (nextIndex < pIndex) {
                // 消费者 < 生产者
                long offset = calcCircularRefElementOffset(nextIndex++, mask);
                E e = lvRefElement(buffer, offset);
                if (e != null) {
                    return e;
                }
            }
            return null;
        }
    }

}


abstract class ConcurrentCircularArrayQueueL0Pad<E> extends AbstractQueue<E> {
    byte b000, b001, b002, b003, b004, b005, b006, b007;//  8b
    byte b010, b011, b012, b013, b014, b015, b016, b017;// 16b
    byte b020, b021, b022, b023, b024, b025, b026, b027;// 24b
    byte b030, b031, b032, b033, b034, b035, b036, b037;// 32b
    byte b040, b041, b042, b043, b044, b045, b046, b047;// 40b
    byte b050, b051, b052, b053, b054, b055, b056, b057;// 48b
    byte b060, b061, b062, b063, b064, b065, b066, b067;// 56b
    byte b070, b071, b072, b073, b074, b075, b076, b077;// 64b
    byte b100, b101, b102, b103, b104, b105, b106, b107;// 72b
    byte b110, b111, b112, b113, b114, b115, b116, b117;// 80b
    byte b120, b121, b122, b123, b124, b125, b126, b127;// 88b
    byte b130, b131, b132, b133, b134, b135, b136, b137;// 96b
    byte b140, b141, b142, b143, b144, b145, b146, b147;//104b
    byte b150, b151, b152, b153, b154, b155, b156, b157;//112b
    byte b160, b161, b162, b163, b164, b165, b166, b167;//120b
    byte b170, b171, b172, b173, b174, b175, b176, b177;//128b
}
