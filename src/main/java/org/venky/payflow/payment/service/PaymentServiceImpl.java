package org.venky.payflow.payment.service;

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
        String hash;
        try {
            hash = requestHashService.generateHash(createPaymentRequest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        IdempotencyCheckResult idempotencyCheckResult = idempotencyService.check(idempotencyKey, hash);

        if (idempotencyCheckResult.getStatus() == IdempotencyCheckStatus.NEW) {

            Payment payment = paymentMapper.toEntity(createPaymentRequest);
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
    public PaymentResponse updatePaymentByPaymentId(CreatePaymentRequest createPaymentRequest) {
        return null;
    }

    @Override
    public PaymentResponse updatePaymentStatusByPaymentId(UUID paymentId, UpdatePaymentStatusRequest updatePaymentStatusRequest) {
        Optional<Payment> payment = paymentRepository.findById(paymentId);

        if (payment.isEmpty()){
            throw  new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        if (validateStatusTransition(payment.get().getStatus(), updatePaymentStatusRequest )){
            payment.get().setStatus(updatePaymentStatusRequest.getStatus());
        }
        payment.get().setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment.get());

        return paymentMapper.toResponse(payment.get());
    }


    private boolean validateStatusTransition(PaymentStatus currentStatus, UpdatePaymentStatusRequest updatePaymentStatusRequest){
        boolean valid = switch (currentStatus){
            case CREATED -> updatePaymentStatusRequest.getStatus() == PaymentStatus.PROCESSING;
            case PROCESSING -> updatePaymentStatusRequest.getStatus() == PaymentStatus.SUCCESS || updatePaymentStatusRequest.getStatus()  == PaymentStatus.FAILED;
            case SUCCESS, FAILED -> false;
        };

        if (!valid){
            throw new InvalidPaymentStatusTransitionException("Invalid payment status transition from " + currentStatus + " to " + updatePaymentStatusRequest.getStatus()  );
        }
        return true;
    }
}
