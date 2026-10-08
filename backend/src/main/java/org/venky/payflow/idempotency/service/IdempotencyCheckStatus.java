package org.venky.payflow.idempotency.service;

public enum IdempotencyCheckStatus {
    NEW,
    RETRY,
    CONFLICT
}