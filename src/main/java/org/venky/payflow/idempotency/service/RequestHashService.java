package org.venky.payflow.idempotency.service;

import org.venky.payflow.payment.dto.CreatePaymentRequest;

import java.security.NoSuchAlgorithmException;

public interface RequestHashService {
    String generateHash(CreatePaymentRequest request) throws NoSuchAlgorithmException;

}
