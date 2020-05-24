package com.ailong.test.executor;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;

/**
 * Created by jiangfei on 2020/5/24.
 */
@Slf4j
public class ExecutorTest {

    @Test
    public void test1() throws IOException {
        TestExecutor testExecutor = new TestExecutor();
        testExecutor.submit(new Runnable() {
            @Override
            public void run() {
                log.info("111");
            }
        });

        System.in.read();
    }

}
