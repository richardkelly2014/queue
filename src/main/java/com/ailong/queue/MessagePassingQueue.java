package com.ailong.queue;

/**
 * 消息经过 队列
 *
 * @param <T>
 */
public interface MessagePassingQueue<T> {

    // 无界的
    int UNBOUNDED_CAPACITY = -1;

    /**
     * 生产
     *
     * @param <T>
     */
    interface Supplier<T> {
        T get();
    }

    /**
     * 消费
     *
     * @param <T>
     */
    interface Consumer<T> {
        void accept(T e);
    }

    /**
     * 等待策略
     */
    interface WaitStrategy {

        int idle(int idleCounter);
    }

    /**
     * 退出条件
     */
    interface ExitCondition {

        boolean keepRunning();
    }

    /**
     * 添加
     *
     * @param e
     * @return
     */
    boolean offer(T e);

    /**
     * 取出
     *
     * @return
     */
    T poll();

    /**
     * 查看，不取出
     *
     * @return
     */
    T peek();

    /**
     * 大小
     *
     * @return
     */
    int size();

    /**
     * 清空
     */
    void clear();

    boolean isEmpty();

    int capacity();

    boolean relaxedOffer(T e);

    T relaxedPoll();

    T relaxedPeek();

    /**
     * 消费
     *
     * @param c
     * @param limit
     * @return
     */
    int drain(Consumer<T> c, int limit);

    /**
     * 填充
     *
     * @param s
     * @param limit
     * @return
     */
    int fill(Supplier<T> s, int limit);

    int drain(Consumer<T> c);

    int fill(Supplier<T> s);

    void drain(Consumer<T> c, WaitStrategy wait, ExitCondition exit);

    void fill(Supplier<T> s, WaitStrategy wait, ExitCondition exit);
}
