package com.ailong.queue;

public final class IndexedQueueSizeUtil {

    /**
     * 队列大小
     *
     * @param iq
     * @return
     */
    public static int size(IndexedQueue iq) {
        /*
         * It is possible for a thread to be interrupted or reschedule between the read of the producer and
         * consumer indices, therefore protection is required to ensure size is within valid range. In the
         * event of concurrent polls/offers to this method the size is OVER estimated as we read consumer
         * index BEFORE the producer index.
         */
        //当前 消费索引
        long after = iq.lvConsumerIndex();
        long size;
        while (true) {
            final long before = after;
            // 当前生产索引
            final long currentProducerIndex = iq.lvProducerIndex();
            // 在取一次消费索引
            after = iq.lvConsumerIndex();
            // 2次相等
            if (before == after) {
                size = (currentProducerIndex - after);
                break;
            }
        }
        // Long overflow is impossible (), so size is always positive. Integer overflow is possible for the unbounded
        // indexed queues.
        if (size > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        } else {
            return (int) size;
        }
    }

    /**
     * 是否位空
     * 消费索引 == 生产索引 = 空
     *
     * @param iq
     * @return
     */
    public static boolean isEmpty(IndexedQueue iq) {
        // Order matters!
        // Loading consumer before producer allows for producer increments after consumer index is read.
        // This ensures this method is conservative in it's estimate. Note that as this is an MPMC there is
        // nothing we can do to make this an exact method.
        return (iq.lvConsumerIndex() == iq.lvProducerIndex());
    }

}
