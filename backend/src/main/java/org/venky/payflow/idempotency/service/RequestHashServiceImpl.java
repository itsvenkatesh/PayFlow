package org.venky.payflow.idempotency.service;

import org.springframework.stereotype.Service;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.TransferMoneyRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class RequestHashServiceImpl
        implements RequestHashService {

    @Override
    public String generateHash(
            CreatePaymentRequest request,
            UUID customerId)
            throws NoSuchAlgorithmException {

        String canonical =
                request.getAmount()
                        + "|"
                        + customerId
                        + "|"
                        + request.getCurrency();

        return generateSha256(canonical);
    }


    @Override
    public String generateTransferHash(
            TransferMoneyRequest request,
            UUID customerId)
            throws NoSuchAlgorithmException {

        String canonical =
                request.getSourceBankAccountNumber()
                        + "|"
                        + request.getDestinationBankAccountNumber()
                        + "|"
                        + request.getAmount()
                        + "|"
                        + request.getCurrency()
                        + "|"
                        + customerId;

        return generateSha256(canonical);
    }


    private String generateSha256(String value)
            throws NoSuchAlgorithmException {

        MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

        byte[] bytes =
                digest.digest(
                        value.getBytes(StandardCharsets.UTF_8)
                );

        StringBuilder result = new StringBuilder();

        for (byte b : bytes) {
            result.append(
                    String.format("%02x", b)
            );
        }

        return result.toString();
    }
}