package com.ailong.disruptor.util;

import com.ailong.disruptor.Sequence;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

/**
 * disruptor util
 * Created by jiangfei on 2020/5/7.
 */
public final class Util {

    private static final Unsafe THE_UNSAFE;

    static {
        try {
            // get unsafe 类
            final PrivilegedExceptionAction<Unsafe> action = new PrivilegedExceptionAction<Unsafe>() {
                public Unsafe run() throws Exception {
                    Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    theUnsafe.setAccessible(true);
                    return (Unsafe) theUnsafe.get(null);
                }
            };

            THE_UNSAFE = AccessController.doPrivileged(action);
        } catch (Exception e) {
            throw new RuntimeException("Unable to load unsafe", e);
        }
    }

    /**
     * get UNSAFE
     *
     * @return
     */
    public static Unsafe getUnsafe() {

        return THE_UNSAFE;
    }

    /**
     * 2的N次方
     *
     * @param x
     * @return
     */
    public static int ceilingNextPowerOfTwo(final int x) {

        return 1 << (32 - Integer.numberOfLeadingZeros(x - 1));
    }

    public static int log2(int i) {
        int r = 0;
        while ((i >>= 1) != 0) {
            ++r;
        }
        return r;
    }

    /**
     * 等待 时间
     *
     * @param mutex
     * @param timeoutNanos
     * @return
     * @throws InterruptedException
     */
    public static long awaitNanos(Object mutex, long timeoutNanos) throws InterruptedException {
        long millis = timeoutNanos / 1_000_000;
        long nanos = timeoutNanos % 1_000_000;

        long t0 = System.nanoTime();
        mutex.wait(millis, (int) nanos);
        long t1 = System.nanoTime();

        return timeoutNanos - (t1 - t0);
    }

    public static long getMinimumSequence(final Sequence[] sequences) {
        return getMinimumSequence(sequences, Long.MAX_VALUE);
    }

    public static long getMinimumSequence(final Sequence[] sequences, long minimum) {
        for (int i = 0, n = sequences.length; i < n; i++) {
            long value = sequences[i].get();
            minimum = Math.min(minimum, value);
        }

        return minimum;
    }
}
