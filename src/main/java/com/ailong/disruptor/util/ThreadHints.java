package com.ailong.disruptor.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * Created by jiangfei on 2020/5/7.
 */
public final class ThreadHints {

    private static final MethodHandle ON_SPIN_WAIT_METHOD_HANDLE;

    static {
        //用于反射
        final MethodHandles.Lookup lookup = MethodHandles.lookup();

        MethodHandle methodHandle = null;
        try {
            // 线程 自旋等待
            methodHandle = lookup.findStatic(Thread.class, "onSpinWait", methodType(void.class));
        } catch (final Exception ignore) {
        }

        ON_SPIN_WAIT_METHOD_HANDLE = methodHandle;
    }

    private ThreadHints() {
    }

    public static void onSpinWait() {
        // Call java.lang.Thread.onSpinWait() on Java SE versions that support it. Do nothing otherwise.
        // This should optimize away to either nothing or to an inlining of java.lang.Thread.onSpinWait()
        if (null != ON_SPIN_WAIT_METHOD_HANDLE) {
            try {
                ON_SPIN_WAIT_METHOD_HANDLE.invokeExact();
            } catch (final Throwable ignore) {
            }
        }
    }
}
