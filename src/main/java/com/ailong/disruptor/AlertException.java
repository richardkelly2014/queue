
package com.ailong.disruptor;

public final class AlertException extends Exception {
    /**
     * Pre-allocated exception to avoid garbage generation
     */
    public static final AlertException INSTANCE = new AlertException();

    /**
     * Private constructor so only a single instance exists.
     */
    private AlertException() {
    }

    /**
     * Overridden so the stack trace is not filled in for this exception for performance reasons.
     *
     * @return this instance.
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
