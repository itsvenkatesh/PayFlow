package org.venky.payflow.payment.service;

import org.venky.payflow.payment.dto.PaymentResponse;

import java.util.UUID;

public interface PaymentCacheService {
    PaymentResponse get(UUID paymentId);

    void put(UUID paymentId, PaymentResponse payment);

    void evict(UUID paymentId);
}
