
package com.ailong.disruptor;


public final class InsufficientCapacityException extends Exception {
    public static final InsufficientCapacityException INSTANCE = new InsufficientCapacityException();

    private InsufficientCapacityException() {
        // Singleton
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
