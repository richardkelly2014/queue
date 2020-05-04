package com.ailong.queue.util;

/**
 * Created by jiangfei on 2019/10/13.
 */

public interface PortableJvmInfo {
    int CACHE_LINE_SIZE = Integer.getInteger("jctools.cacheLineSize", 64);
    int CPUs = Runtime.getRuntime().availableProcessors();
    int RECOMENDED_OFFER_BATCH = CPUs * 4;
    int RECOMENDED_POLL_BATCH = CPUs * 4;
}
