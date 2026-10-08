package org.venky.payflow.idempotency.service;

import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.TransferMoneyRequest;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public interface RequestHashService {

    String generateHash(
            CreatePaymentRequest request,
            UUID customerId
    ) throws NoSuchAlgorithmException;

    String generateTransferHash(
            TransferMoneyRequest request,
            UUID customerId
    ) throws NoSuchAlgorithmException;
}