package com.ailong.queue;

import static com.ailong.queue.util.UnsafeAccess.UNSAFE;
import static com.ailong.queue.util.UnsafeAccess.fieldOffset;
import static com.ailong.queue.util.UnsafeRefArrayAccess.calcCircularRefElementOffset;
import static com.ailong.queue.util.UnsafeRefArrayAccess.lvRefElement;
import static com.ailong.queue.util.UnsafeRefArrayAccess.soRefElement;

/**
 * 无锁有界 队列
 * 多生产者--单消费者
 *
 * @param <E>
 */
public class MpscArrayQueue<E> extends MpscArrayQueueL3Pad<E> {

    public MpscArrayQueue(final int capacity) {

        super(capacity);
    }

    /**
     * 添加
     *
     * @param e
     * @return
     */
    @Override
    public boolean offer(final E e) {
        if (null == e) {
            throw new NullPointerException();
        }

        final long mask = this.mask; // mask
        long producerLimit = lvProducerLimit(); // limit
        long pIndex;

        do {
            //循环处理 生产者索引
            //先做一次 limit 判断 并 扩大 limit
            //p index
            pIndex = lvProducerIndex();
            //生产者index >= limit
            if (pIndex >= producerLimit) {

                //消费者 索引
                final long cIndex = lvConsumerIndex();
                producerLimit = cIndex + mask + 1;

                if (pIndex >= producerLimit) {
                    return false; // FULL :(
                } else {
                    // update producer limit to the next index that we must recheck the consumer index
                    // this is racy, but the race is benign
                    soProducerLimit(producerLimit);
                }
            }

        } while (!casProducerIndex(pIndex, pIndex + 1)); //cas 操作生产者index

        final long offset = calcCircularRefElementOffset(pIndex, mask);
        soRefElement(buffer, offset, e);
        return true; // AWESOME :)
    }

    /**
     * 取出数据
     *
     * @return
     */
    @Override
    public E poll() {
        // 消费者 索引
        final long cIndex = lpConsumerIndex();
        // 消费者 索引 offset
        final long offset = calcCircularRefElementOffset(cIndex, mask);

        final E[] buffer = this.buffer;

        // If we can't see the next available element we can't poll
        E e = lvRefElement(buffer, offset);
        if (null == e) {
            /*
             * NOTE: Queue may not actually be empty in the case of a producer (P1) being interrupted after
             * winning the CAS on offer but before storing the element in the queue. Other producers may go on
             * to fill up the queue after this element.
             */
            if (cIndex != lvProducerIndex()) {
                // 消费索引和 生产者索引
                do {
                    e = lvRefElement(buffer, offset);
                }
                while (e == null);
            } else {
                return null;
            }
        }
        //取出的位置设置为null，为gc回收处理
        soRefElement(buffer, offset, null);
        soConsumerIndex(cIndex + 1);
        return e;
    }

    @Override
    public E peek() {
        final E[] buffer = this.buffer;

        final long cIndex = lpConsumerIndex();
        final long offset = calcCircularRefElementOffset(cIndex, mask);
        E e = lvRefElement(buffer, offset);
        if (null == e) {
            /*
             * NOTE: Queue may not actually be empty in the case of a producer (P1) being interrupted after
             * winning the CAS on offer but before storing the element in the queue. Other producers may go on
             * to fill up the queue after this element.
             */
            if (cIndex != lvProducerIndex()) {
                do {
                    e = lvRefElement(buffer, offset);
                }
                while (e == null);
            } else {
                return null;
            }
        }
        return e;
    }

    @Override
    public boolean relaxedOffer(E e) {
        return offer(e);
    }

    @Override
    public E relaxedPoll() {
        final E[] buffer = this.buffer;
        final long cIndex = lpConsumerIndex();
        final long offset = calcCircularRefElementOffset(cIndex, mask);

        // If we can't see the next available element we can't poll
        E e = lvRefElement(buffer, offset);
        if (null == e) {
            return null;
        }

        soRefElement(buffer, offset, null);
        soConsumerIndex(cIndex + 1);
        return e;
    }

    @Override
    public E relaxedPeek() {
        final E[] buffer = this.buffer;
        final long mask = this.mask;
        final long cIndex = lpConsumerIndex();
        return lvRefElement(buffer, calcCircularRefElementOffset(cIndex, mask));
    }

    @Override
    public int drain(final Consumer<E> c, final int limit) {
        if (null == c)
            throw new IllegalArgumentException("c is null");
        if (limit < 0)
            throw new IllegalArgumentException("limit is negative: " + limit);
        if (limit == 0)
            return 0;

        final E[] buffer = this.buffer;
        final long mask = this.mask;
        final long cIndex = lpConsumerIndex();

        for (int i = 0; i < limit; i++) {
            final long index = cIndex + i;
            final long offset = calcCircularRefElementOffset(index, mask);
            final E e = lvRefElement(buffer, offset);
            if (null == e) {
                return i;
            }
            soRefElement(buffer, offset, null);
            soConsumerIndex(index + 1); // ordered store -> atomic and ordered for size()
            c.accept(e);
        }
        return limit;
    }

    @Override
    public int fill(Supplier<E> s, int limit) {
        if (null == s)
            throw new IllegalArgumentException("supplier is null");
        if (limit < 0)
            throw new IllegalArgumentException("limit is negative:" + limit);
        if (limit == 0)
            return 0;

        final long mask = this.mask;
        final long capacity = mask + 1;
        long producerLimit = lvProducerLimit();
        long pIndex;
        int actualLimit = 0;
        do {
            pIndex = lvProducerIndex();
            long available = producerLimit - pIndex;
            if (available <= 0) {
                final long cIndex = lvConsumerIndex();
                producerLimit = cIndex + capacity;
                available = producerLimit - pIndex;
                if (available <= 0) {
                    return 0; // FULL :(
                } else {
                    // update producer limit to the next index that we must recheck the consumer index
                    soProducerLimit(producerLimit);
                }
            }
            actualLimit = Math.min((int) available, limit);
        }
        while (!casProducerIndex(pIndex, pIndex + actualLimit));
        // right, now we claimed a few slots and can fill them with goodness
        final E[] buffer = this.buffer;
        for (int i = 0; i < actualLimit; i++) {
            // Won CAS, move on to storing
            final long offset = calcCircularRefElementOffset(pIndex + i, mask);
            soRefElement(buffer, offset, s.get());
        }
        return actualLimit;
    }

    @Override
    public int drain(Consumer<E> c) {
        return drain(c, capacity());
    }

    @Override
    public int fill(Supplier<E> s) {
        return MessagePassingQueueUtil.fillBounded(this, s);
    }

    @Override
    public void drain(Consumer<E> c, WaitStrategy w, ExitCondition exit) {
        MessagePassingQueueUtil.drain(this, c, w, exit);
    }

    @Override
    public void fill(Supplier<E> s, WaitStrategy wait, ExitCondition exit) {
        MessagePassingQueueUtil.fill(this, s, wait, exit);
    }
}

abstract class MpscArrayQueueL3Pad<E> extends MpscArrayQueueConsumerIndexField<E> {
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

    MpscArrayQueueL3Pad(int capacity) {
        super(capacity);
    }
}

// 消费者 索引
abstract class MpscArrayQueueConsumerIndexField<E> extends MpscArrayQueueL2Pad<E> {
    private final static long C_INDEX_OFFSET = fieldOffset(MpscArrayQueueConsumerIndexField.class, "consumerIndex");

    private volatile long consumerIndex;

    MpscArrayQueueConsumerIndexField(int capacity) {

        super(capacity);
    }

    @Override
    public final long lvConsumerIndex() {
        return consumerIndex;
    }

    /**
     * get
     *
     * @return
     */
    final long lpConsumerIndex() {
        return UNSAFE.getLong(this, C_INDEX_OFFSET);
    }

    /**
     * set
     *
     * @param newValue
     */
    final void soConsumerIndex(long newValue) {
        UNSAFE.putOrderedLong(this, C_INDEX_OFFSET, newValue);
    }
}

abstract class MpscArrayQueueL2Pad<E> extends MpscArrayQueueProducerLimitField<E> {
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
    // byte b170,b171,b172,b173,b174,b175,b176,b177;//128b

    MpscArrayQueueL2Pad(int capacity) {
        super(capacity);
    }
}

// 生产者 limit 容量
abstract class MpscArrayQueueProducerLimitField<E> extends MpscArrayQueueMidPad<E> {
    private final static long P_LIMIT_OFFSET = fieldOffset(MpscArrayQueueProducerLimitField.class, "producerLimit");

    // First unavailable index the producer may claim up to before rereading the consumer index
    private volatile long producerLimit;

    MpscArrayQueueProducerLimitField(int capacity) {
        super(capacity);
        this.producerLimit = capacity;
    }

    /**
     * get limit
     *
     * @return
     */
    final long lvProducerLimit() {
        return producerLimit;
    }

    /**
     * set limit
     *
     * @param newValue
     */
    final void soProducerLimit(long newValue) {
        UNSAFE.putOrderedLong(this, P_LIMIT_OFFSET, newValue);
    }
}

// mid pad 中间
abstract class MpscArrayQueueMidPad<E> extends MpscArrayQueueProducerIndexField<E> {
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

    MpscArrayQueueMidPad(int capacity) {
        super(capacity);
    }
}


// 生产者 索引
abstract class MpscArrayQueueProducerIndexField<E> extends MpscArrayQueueL1Pad<E> {
    private final static long P_INDEX_OFFSET = fieldOffset(MpscArrayQueueProducerIndexField.class, "producerIndex");

    private volatile long producerIndex;

    MpscArrayQueueProducerIndexField(int capacity) {
        super(capacity);
    }

    @Override
    public final long lvProducerIndex() {
        return producerIndex;
    }

    /**
     * cas 操作
     * 利用 unsafe 原子操作
     *
     * @param expect
     * @param newValue
     * @return
     */
    final boolean casProducerIndex(long expect, long newValue) {
        return UNSAFE.compareAndSwapLong(this, P_INDEX_OFFSET, expect, newValue);
    }
}

abstract class MpscArrayQueueL1Pad<E> extends ConcurrentCircularArrayQueue<E> {
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
    // byte b170,b171,b172,b173,b174,b175,b176,b177;//128b

    MpscArrayQueueL1Pad(int capacity) {
        super(capacity);
    }
}