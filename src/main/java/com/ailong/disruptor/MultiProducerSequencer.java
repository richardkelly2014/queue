package com.ailong.disruptor;

import com.ailong.disruptor.util.Util;
import sun.misc.Unsafe;

import java.util.concurrent.locks.LockSupport;

/**
 * 多生产者 序列器
 */
public final class MultiProducerSequencer extends AbstractSequencer {

    private static final Unsafe UNSAFE = Util.getUnsafe();
    private static final long BASE = UNSAFE.arrayBaseOffset(int[].class);
    private static final long SCALE = UNSAFE.arrayIndexScale(int[].class);

    // 序列缓存
    private final Sequence gatingSequenceCache = new Sequence(Sequencer.INITIAL_CURSOR_VALUE);

    // 可用buffer
    private final int[] availableBuffer;
    private final int indexMask;
    private final int indexShift;


    public MultiProducerSequencer(int bufferSize, final WaitStrategy waitStrategy) {
        super(bufferSize, waitStrategy);

        //buffer
        availableBuffer = new int[bufferSize];
        //mask
        indexMask = bufferSize - 1;
        //2的n次方那个数
        indexShift = Util.log2(bufferSize);
        //初始化值
        initialiseAvailableBuffer();
    }

    /**
     * 可用容量
     *
     * @param requiredCapacity
     * @return
     */
    @Override
    public boolean hasAvailableCapacity(final int requiredCapacity) {

        return hasAvailableCapacity(gatingSequences, requiredCapacity, cursor.get());
    }

    private boolean hasAvailableCapacity(Sequence[] gatingSequences, final int requiredCapacity, long cursorValue) {
        // 游标 + 需要容量 - bufferSize
        long wrapPoint = (cursorValue + requiredCapacity) - bufferSize;
        // 缓存
        long cachedGatingSequence = gatingSequenceCache.get();

        if (wrapPoint > cachedGatingSequence || cachedGatingSequence > cursorValue) {
            long minSequence = Util.getMinimumSequence(gatingSequences, cursorValue);
            gatingSequenceCache.set(minSequence);

            if (wrapPoint > minSequence) {
                return false;
            }
        }

        return true;
    }


    @Override
    public void claim(long sequence) {
        // 游标 设置
        cursor.set(sequence);
    }

    @Override
    public long next() {

        return next(1);
    }

    @Override
    public long next(int n) {
        if (n < 1 || n > bufferSize) {
            throw new IllegalArgumentException("n must be > 0 and < bufferSize");
        }

        long current;
        long next;

        do {
            // cas 循环处理
            // 当前游标
            current = cursor.get();
            // 下一个位置
            next = current + n;

            // 下一个位置 - bufferSize
            long wrapPoint = next - bufferSize;
            // 阀门 序列缓存
            long cachedGatingSequence = gatingSequenceCache.get();
            //
            if (wrapPoint > cachedGatingSequence || cachedGatingSequence > current) {
                long gatingSequence = Util.getMinimumSequence(gatingSequences, current);

                if (wrapPoint > gatingSequence) {
                    LockSupport.parkNanos(1);
                    continue;
                }

                gatingSequenceCache.set(gatingSequence);
            } else if (cursor.compareAndSet(current, next)) {
                break;
            }
        } while (true);

        return next;
    }

    @Override
    public long tryNext() throws InsufficientCapacityException {
        return tryNext(1);
    }

    @Override
    public long tryNext(int n) throws InsufficientCapacityException {
        if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }

        long current;
        long next;

        do {
            // 当前游标
            current = cursor.get();
            // 下一个
            next = current + n;

            if (!hasAvailableCapacity(gatingSequences, n, current)) {
                throw InsufficientCapacityException.INSTANCE;
            }
        }
        while (!cursor.compareAndSet(current, next));

        return next;
    }

    @Override
    public long remainingCapacity() {
        long consumed = Util.getMinimumSequence(gatingSequences, cursor.get());
        long produced = cursor.get();
        return getBufferSize() - (produced - consumed);
    }

    private void initialiseAvailableBuffer() {
        for (int i = availableBuffer.length - 1; i != 0; i--) {
            setAvailableBufferValue(i, -1);
        }

        setAvailableBufferValue(0, -1);
    }

    @Override
    public void publish(final long sequence) {
        setAvailable(sequence);
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    public void publish(long lo, long hi) {
        for (long l = lo; l <= hi; l++) {
            setAvailable(l);
        }
        waitStrategy.signalAllWhenBlocking();
    }

    @Override
    public boolean isAvailable(long sequence) {
        int index = calculateIndex(sequence);
        int flag = calculateAvailabilityFlag(sequence);
        long bufferAddress = (index * SCALE) + BASE;
        return UNSAFE.getIntVolatile(availableBuffer, bufferAddress) == flag;
    }

    @Override
    public long getHighestPublishedSequence(long lowerBound, long availableSequence) {
        for (long sequence = lowerBound; sequence <= availableSequence; sequence++) {
            if (!isAvailable(sequence)) {
                return sequence - 1;
            }
        }

        return availableSequence;
    }

    private void setAvailable(final long sequence) {
        setAvailableBufferValue(calculateIndex(sequence), calculateAvailabilityFlag(sequence));
    }

    /**
     * 设置 值
     *
     * @param index
     * @param flag
     */
    private void setAvailableBufferValue(int index, int flag) {
        long bufferAddress = (index * SCALE) + BASE;
        UNSAFE.putOrderedInt(availableBuffer, bufferAddress, flag);
    }

    private int calculateAvailabilityFlag(final long sequence) {
        return (int) (sequence >>> indexShift);
    }

    //圆圈 计算索引
    private int calculateIndex(final long sequence) {
        return ((int) sequence) & indexMask;
    }

}
