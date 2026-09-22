package org.venky.payflow.idempotency.service;

import org.springframework.stereotype.Service;
import org.venky.payflow.payment.dto.CreatePaymentRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class RequestHashServiceImpl implements RequestHashService{
    @Override
    public String generateHash(CreatePaymentRequest request, UUID customerId) throws NoSuchAlgorithmException {
        String canonical = request.getAmount().toString()
                + "|" + customerId
                + "|" + request.getCurrency();

        byte[] bytes = canonical.getBytes(StandardCharsets.UTF_8);
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
