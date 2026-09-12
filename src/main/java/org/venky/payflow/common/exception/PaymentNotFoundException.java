package org.venky.payflow.common.exception;


public class PaymentNotFoundException extends RuntimeException {
    public PaymentNotFoundException() {
    }
    public PaymentNotFoundException(String message) {
        super(message);
    }

}
