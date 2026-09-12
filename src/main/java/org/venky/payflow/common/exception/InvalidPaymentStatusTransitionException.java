package org.venky.payflow.common.exception;

public class InvalidPaymentStatusTransitionException extends RuntimeException {

    public InvalidPaymentStatusTransitionException() {
    }
    public InvalidPaymentStatusTransitionException(String message) {
        super(message);
    }

}
