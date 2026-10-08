package org.venky.payflow.idempotency.service;

import org.venky.payflow.idempotency.entity.IdempotencyRecord;

import java.util.UUID;

public interface IdempotencyService {
    public IdempotencyCheckResult check(String idempotencyKey, String requestHash);

    public IdempotencyRecord saveRecord( String idempotencyKey,String hash , UUID paymentId);
}
