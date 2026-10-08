package org.venky.payflow.payment.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.dto.TransferMoneyRequest;
import org.venky.payflow.payment.dto.TransferMoneyResponse;
import org.venky.payflow.payment.dto.UpdatePaymentStatusRequest;
import org.venky.payflow.payment.service.PaymentService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransferMoneyResponse transferMoney(
            @Valid @RequestBody TransferMoneyRequest request,
            @RequestHeader("idempotency-key") String idempotencyKey) {

        return paymentService.transferMoney(
                request,
                idempotencyKey
        );
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse updatePaymentStatus(
            @PathVariable UUID paymentId,
            @RequestBody UpdatePaymentStatusRequest request) {

        return paymentService.updatePaymentStatusByPaymentId(
                paymentId,
                request
        );
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse refundPayment(
            @PathVariable UUID paymentId) {

        return paymentService.refundPayment(
                paymentId
        );
    }

    @PatchMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse updateRefundStatus(
            @PathVariable UUID paymentId,
            @RequestBody UpdatePaymentStatusRequest request) {

        return paymentService.processRefund(
                paymentId,
                request
        );
    }
}