package com.ailong.test;

import com.ailong.queue.util.Pow2;
import com.ailong.queue.util.UnsafeLongArrayAccess;
import com.ailong.queue.util.UnsafeRefArrayAccess;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class BaseTest {

    @Test
    public void test1() {
        log.info("{}", 111);

        log.info("{}", Pow2.roundToPowerOfTwo(9));

        log.info("{}", UnsafeRefArrayAccess.REF_ARRAY_BASE);

        log.info("{}", UnsafeLongArrayAccess.LONG_ARRAY_BASE);
    }

    @Test
    public void test2() {
        log.info("{}", 111);
    }

    @Test
    public void test3() {
        long current = System.currentTimeMillis();
        log.info("{}", current / 1_000);
    }

    @Test
    public void test4() {
        log.info("{}", System.getProperty("java.specification.version"));
        log.info("{}", System.getProperty("java.vm.name"));
        log.info("{}", System.getProperty("os.arch"));
    }

}
