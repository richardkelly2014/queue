package com.ailong.disruptor;

import com.ailong.disruptor.dsl.ProducerType;
import com.ailong.disruptor.util.Util;
import sun.misc.Unsafe;

//缓存填充行
abstract class RingBufferPad {
    protected long p1, p2, p3, p4, p5, p6, p7;
}

abstract class RingBufferFields<E> extends RingBufferPad {

    // 缓冲区
    private static final int BUFFER_PAD;
    //数组开始位置
    private static final long REF_ARRAY_BASE;
    // 偏移量
    private static final int REF_ELEMENT_SHIFT;
    private static final Unsafe UNSAFE = Util.getUnsafe();

    static {
        final int scale = UNSAFE.arrayIndexScale(Object[].class);
        if (4 == scale) {
            REF_ELEMENT_SHIFT = 2;
        } else if (8 == scale) {
            REF_ELEMENT_SHIFT = 3;
        } else {
            throw new IllegalStateException("Unknown pointer size");
        }
        //缓冲区
        BUFFER_PAD = 128 / scale;
        // Including the buffer pad in the array base offset
        //开始位置=缓冲区+base
        REF_ARRAY_BASE = UNSAFE.arrayBaseOffset(Object[].class) + 128;
    }

    // mask
    private final long indexMask;
    private final Object[] entries;
    protected final int bufferSize;
    protected final Sequencer sequencer;

    RingBufferFields(
            EventFactory<E> eventFactory,
            Sequencer sequencer) {
        //序列器
        this.sequencer = sequencer;
        //缓冲区大小
        this.bufferSize = sequencer.getBufferSize();

        if (bufferSize < 1) {
            throw new IllegalArgumentException("bufferSize must not be less than 1");
        }
        if (Integer.bitCount(bufferSize) != 1) {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }
        // mask
        this.indexMask = bufferSize - 1;
        // 实体对象缓存=bufferSize + 2*缓冲区
        this.entries = new Object[sequencer.getBufferSize() + 2 * BUFFER_PAD];
        fill(eventFactory);
    }

    //填充 实体对象
    private void fill(EventFactory<E> eventFactory) {
        for (int i = 0; i < bufferSize; i++) {
            //缓冲区之后开始
            entries[BUFFER_PAD + i] = eventFactory.newInstance();
        }
    }

    @SuppressWarnings("unchecked")
    protected final E elementAt(long sequence) {
        // 缓冲区
        return (E) UNSAFE.getObject(entries, REF_ARRAY_BASE + ((sequence & indexMask) << REF_ELEMENT_SHIFT));
    }
}

public final class RingBuffer<E> extends RingBufferFields<E> implements Cursored, EventSequencer<E>, EventSink<E> {

    public static final long INITIAL_CURSOR_VALUE = Sequence.INITIAL_VALUE;
    protected long p1, p2, p3, p4, p5, p6, p7;

    RingBuffer(
            EventFactory<E> eventFactory,
            Sequencer sequencer) {
        super(eventFactory, sequencer);
    }

    public static <E> RingBuffer<E> createMultiProducer(
            EventFactory<E> factory,
            int bufferSize,
            WaitStrategy waitStrategy) {
        //多生产者
        MultiProducerSequencer sequencer = new MultiProducerSequencer(bufferSize, waitStrategy);

        return new RingBuffer<E>(factory, sequencer);
    }

    public static <E> RingBuffer<E> create(
            ProducerType producerType,
            EventFactory<E> factory,
            int bufferSize,
            WaitStrategy waitStrategy) {
        switch (producerType) {
            case SINGLE:
                //return createSingleProducer(factory, bufferSize, waitStrategy);
            case MULTI:
                return createMultiProducer(factory, bufferSize, waitStrategy);
            default:
                throw new IllegalStateException(producerType.toString());
        }
    }

    /**
     * 获取 序列相对应的元素
     *
     * @param sequence
     * @return
     */
    @Override
    public E get(long sequence) {

        return elementAt(sequence);
    }

    /**
     * 获取下一个序列
     *
     * @return
     */
    @Override
    public long next() {

        return sequencer.next();
    }

    @Override
    public long next(int n) {
        return sequencer.next(n);
    }

    @Override
    public long tryNext() throws InsufficientCapacityException {
        return sequencer.tryNext();
    }

    @Override
    public long tryNext(int n) throws InsufficientCapacityException {
        return sequencer.tryNext(n);
    }

    @Override
    public long getCursor() {
        return sequencer.getCursor();
    }

    public SequenceBarrier newBarrier(Sequence... sequencesToTrack) {
        return sequencer.newBarrier(sequencesToTrack);
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public boolean hasAvailableCapacity(int requiredCapacity) {
        return sequencer.hasAvailableCapacity(requiredCapacity);
    }

    @Override
    public void publish(long sequence) {
        sequencer.publish(sequence);
    }

    @Override
    public void publish(long lo, long hi) {
        sequencer.publish(lo, hi);
    }

    public long remainingCapacity() {
        return sequencer.remainingCapacity();
    }

    @Override
    public String toString() {
        return "RingBuffer{" +
                "bufferSize=" + bufferSize +
                ", sequencer=" + sequencer +
                "}";
    }
}
