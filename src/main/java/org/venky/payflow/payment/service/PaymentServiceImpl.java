package org.venky.payflow.payment.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.venky.payflow.common.exception.IdempotencyConflictException;
import org.venky.payflow.common.exception.InvalidPaymentStatusTransitionException;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.idempotency.service.IdempotencyCheckResult;
import org.venky.payflow.idempotency.service.IdempotencyCheckStatus;
import org.venky.payflow.idempotency.service.IdempotencyService;
import org.venky.payflow.idempotency.service.RequestHashService;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.dto.UpdatePaymentStatusRequest;
import org.venky.payflow.payment.entity.Payment;
import org.venky.payflow.payment.enums.PaymentStatus;
import org.venky.payflow.payment.mapper.PaymentMapper;
import org.venky.payflow.payment.repository.PaymentRepository;
import org.venky.payflow.user.security.AuthenticatedUser;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper paymentMapper;
    private final PaymentRepository paymentRepository;
    private final RequestHashService requestHashService;
    private final IdempotencyService idempotencyService;

    public PaymentServiceImpl(PaymentMapper paymentMapper, PaymentRepository paymentRepository, RequestHashService requestHashService, IdempotencyService idempotencyService) {
        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.requestHashService = requestHashService;
        this.idempotencyService = idempotencyService;
    }

    @Transactional
    @Override
    public PaymentResponse createPaymentRequest(CreatePaymentRequest createPaymentRequest, String idempotencyKey) {
        UUID customerId = extractUserIdFromAuthentication();
        String hash;
        try {
            hash = requestHashService.generateHash(createPaymentRequest, customerId);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        IdempotencyCheckResult idempotencyCheckResult = idempotencyService.check(idempotencyKey, hash);

        if (idempotencyCheckResult.getStatus() == IdempotencyCheckStatus.NEW) {
            Payment payment = paymentMapper.toEntity(createPaymentRequest);
            payment.setCustomerId(customerId);
            payment.setStatus(PaymentStatus.CREATED);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setUpdatedAt(LocalDateTime.now());

            Payment paymentResponse = paymentRepository.save(payment);

            idempotencyService.saveRecord(idempotencyKey, hash, paymentResponse.getId());

            return paymentMapper.toResponse(paymentResponse);
        } else if (idempotencyCheckResult.getStatus() == IdempotencyCheckStatus.RETRY) {
            Optional<Payment> payment=  paymentRepository.findById(idempotencyCheckResult.getPaymentId());
            if (payment.isPresent()) {
                return paymentMapper.toResponse(payment.get());
            }throw new ResourceNotFoundException("Payment with id " + idempotencyCheckResult.getPaymentId() + " not found");
        }else{
            throw new IdempotencyConflictException("Idempotency key has already been used with a different request");
        }
    }

    @Override
    public List<PaymentResponse> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();

        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }


    @Override
    public PaymentResponse getPaymentByPaymentId(UUID  paymentId) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);

        if (payment.isPresent()){
            return paymentMapper.toResponse(payment.get());
        }
        throw  new ResourceNotFoundException("Payment not found with payment id " + paymentId);
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId) {
        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);

        if (paymentOptional.isEmpty()){
            throw  new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }
        Payment payment = paymentOptional.get();

        UUID authenticatedUserId = extractUserIdFromAuthentication();

        if (!payment.getCustomerId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You are not allowed to refund this payment");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStatusTransitionException(
                    "Payment cannot be refunded from status " + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.REFUND_PENDING);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse processRefund(UUID paymentId, UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isEmpty()){
            throw  new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        Payment payment = optionalPayment.get();

        validateStatusTransition(payment.getStatus(), updatePaymentStatusRequest);
        payment.setStatus(updatePaymentStatusRequest.getStatus());

        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);
        return paymentMapper.toResponse(payment);

    }


    @Override
    public PaymentResponse updatePaymentStatusByPaymentId(UUID paymentId, UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);

        if (payment.isEmpty()){
            throw  new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        validateStatusTransition(payment.get().getStatus(), updatePaymentStatusRequest );
        payment.get().setStatus(updatePaymentStatusRequest.getStatus());

        payment.get().setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment.get());

        return paymentMapper.toResponse(payment.get());
    }


    private void validateStatusTransition(PaymentStatus currentStatus, UpdatePaymentStatusRequest updatePaymentStatusRequest){
        boolean valid = switch (currentStatus){
            case CREATED -> updatePaymentStatusRequest.getStatus() == PaymentStatus.PROCESSING;
            case PROCESSING -> updatePaymentStatusRequest.getStatus() == PaymentStatus.SUCCESS || updatePaymentStatusRequest.getStatus()  == PaymentStatus.FAILED;
            case SUCCESS -> updatePaymentStatusRequest.getStatus() == PaymentStatus.REFUND_PENDING;
            case REFUND_PENDING ->  updatePaymentStatusRequest.getStatus() == PaymentStatus.REFUNDED || updatePaymentStatusRequest.getStatus()  == PaymentStatus.REFUND_REJECTED;
            case FAILED, REFUNDED, REFUND_REJECTED -> false;
        };

        if (!valid){
            throw new InvalidPaymentStatusTransitionException("Invalid payment status transition from " + currentStatus + " to " + updatePaymentStatusRequest.getStatus()  );
        }
    }

    private UUID extractUserIdFromAuthentication() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {

            throw new InsufficientAuthenticationException("User is not authenticated");
        }

        return authenticatedUser.userId();
    }
}
