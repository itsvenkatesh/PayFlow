package org.venky.payflow.payment.service;

import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.dto.UpdatePaymentStatusRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    public PaymentResponse createPaymentRequest(CreatePaymentRequest createPaymentRequest, String idempotencyKey);

    public List<PaymentResponse> getAllPayments();

    public PaymentResponse getPaymentByPaymentId(UUID paymentId);

    public PaymentResponse updatePaymentByPaymentId(CreatePaymentRequest createPaymentRequest);

    public PaymentResponse updatePaymentStatusByPaymentId(UUID paymentId, UpdatePaymentStatusRequest updatePaymentStatusRequest);
}
