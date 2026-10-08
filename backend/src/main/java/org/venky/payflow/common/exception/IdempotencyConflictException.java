package org.venky.payflow.common.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
    }
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
