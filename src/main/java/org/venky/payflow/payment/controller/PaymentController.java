package org.venky.payflow.payment.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
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


    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse createPayment(@Valid @RequestBody CreatePaymentRequest createPaymentRequest, @RequestHeader("idempotency-key") String idempotencyKey){
        return paymentService.createPaymentRequest(createPaymentRequest, idempotencyKey);
    }

    @GetMapping
    public List<PaymentResponse> getAllPayments(){
        return paymentService.getAllPayments();
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(@PathVariable UUID paymentId){
        return paymentService.getPaymentByPaymentId(paymentId);
    }

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse updatePaymentStatus(@PathVariable UUID paymentId, @RequestBody UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        return paymentService.updatePaymentStatusByPaymentId(paymentId, updatePaymentStatusRequest);
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('CUSTOMER')")
    public PaymentResponse refundPayment(@PathVariable UUID paymentId) {
        return paymentService.refundPayment(paymentId);
    }

    @PatchMapping("/{paymentId}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public PaymentResponse updateRefundStatus(@PathVariable UUID paymentId, @RequestBody UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        return paymentService.updatePaymentStatusByPaymentId(paymentId, updatePaymentStatusRequest);
    }
}
