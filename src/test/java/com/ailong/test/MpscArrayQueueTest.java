package com.ailong.test;

import com.ailong.queue.MpscArrayQueue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class MpscArrayQueueTest {

    MpscArrayQueue<Integer> queue = new MpscArrayQueue<>(1024 * 1024);

    @Test
    public void singleWrite() throws InterruptedException {

        Thread read = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Integer v = queue.poll();
                    if (v == null) {
                        break;
                    }
                }
            }
        });

        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        CountDownLatch countDownLatch1 = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < 100000; i++) {
                        queue.offer(i);
                    }

                    countDownLatch1.countDown();
                }
            });

            thread.start();
        }

        //同时启动10个线程
        countDownLatch.countDown();

        //等待10个线程执行完毕
        countDownLatch1.await();
        log.info("111 {} {}", queue.size(), (System.currentTimeMillis() - start));
        Thread.sleep(1000);
        read.start();

        read.join();
        log.info("{} {}", queue.size(), (System.currentTimeMillis() - start));
    }


}
