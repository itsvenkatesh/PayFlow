package org.venky.payflow.payment.service;

import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.dto.TransferMoneyRequest;
import org.venky.payflow.payment.dto.TransferMoneyResponse;
import org.venky.payflow.payment.dto.UpdatePaymentStatusRequest;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    TransferMoneyResponse transferMoney(
            TransferMoneyRequest request,
            String idempotencyKey
    );

    List<PaymentResponse> getAllPayments();

    PaymentResponse getPaymentByPaymentId(UUID paymentId);

    PaymentResponse refundPayment(UUID paymentId);

    PaymentResponse processRefund(
            UUID paymentId,
            UpdatePaymentStatusRequest updatePaymentStatusRequest
    );

    PaymentResponse updatePaymentStatusByPaymentId(
            UUID paymentId,
            UpdatePaymentStatusRequest updatePaymentStatusRequest
    );
}