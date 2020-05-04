package com.ailong.queue.util;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * Unsafe 类 操作
 * Created by jiangfei on 2019/10/13.
 */
public final class UnsafeAccess {

    public static final boolean SUPPORTS_GET_AND_SET;
    public static final Unsafe UNSAFE;

    static {
        Unsafe instance;
        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            instance = (Unsafe) field.get(null);
        } catch (Exception ignored) {
            try {
                Constructor<Unsafe> c = Unsafe.class.getDeclaredConstructor();
                c.setAccessible(true);
                instance = c.newInstance();
            } catch (Exception e) {
                SUPPORTS_GET_AND_SET = false;
                throw new RuntimeException(e);
            }
        }

        boolean getAndSetSupport = false;
        try {
            Unsafe.class.getMethod("getAndSetObject", Object.class, Long.TYPE, Object.class);
            getAndSetSupport = true;
        } catch (Exception ignored) {
        }

        UNSAFE = instance;
        SUPPORTS_GET_AND_SET = getAndSetSupport;
    }

    /**
     * 获取 类 成员变量 offset
     *
     * @param clz
     * @param fieldName
     * @return
     * @throws RuntimeException
     */
    public static long fieldOffset(Class clz, String fieldName) throws RuntimeException {
        try {
            return UNSAFE.objectFieldOffset(clz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

}
