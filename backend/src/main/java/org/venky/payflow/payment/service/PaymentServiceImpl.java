package org.venky.payflow.payment.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.venky.payflow.bankaccount.entity.BankAccount;
import org.venky.payflow.bankaccount.enums.BankAccountStatus;
import org.venky.payflow.bankaccount.repository.BankAccountRepository;
import org.venky.payflow.common.exception.IdempotencyConflictException;
import org.venky.payflow.common.exception.InvalidPaymentStatusTransitionException;
import org.venky.payflow.common.exception.ResourceNotFoundException;
import org.venky.payflow.common.security.CurrentUserService;
import org.venky.payflow.idempotency.service.IdempotencyCheckResult;
import org.venky.payflow.idempotency.service.IdempotencyCheckStatus;
import org.venky.payflow.idempotency.service.IdempotencyService;
import org.venky.payflow.idempotency.service.RequestHashService;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.dto.TransferMoneyRequest;
import org.venky.payflow.payment.dto.TransferMoneyResponse;
import org.venky.payflow.payment.dto.UpdatePaymentStatusRequest;
import org.venky.payflow.payment.entity.Payment;
import org.venky.payflow.payment.enums.PaymentStatus;
import org.venky.payflow.payment.mapper.PaymentMapper;
import org.venky.payflow.payment.repository.PaymentRepository;
import org.venky.payflow.transaction.dto.TransactionResponse;
import org.venky.payflow.transaction.service.TransactionService;

import java.math.BigDecimal;
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
    private final PaymentCacheService paymentCacheService;
    private final CurrentUserService currentUserService;
    private final BankAccountRepository bankAccountRepository;
    private final TransactionService transactionService;

    public PaymentServiceImpl(
            PaymentMapper paymentMapper,
            PaymentRepository paymentRepository,
            RequestHashService requestHashService,
            IdempotencyService idempotencyService,
            PaymentCacheService paymentCacheService,
            CurrentUserService currentUserService,
            BankAccountRepository bankAccountRepository,
            TransactionService transactionService) {

        this.paymentMapper = paymentMapper;
        this.paymentRepository = paymentRepository;
        this.requestHashService = requestHashService;
        this.idempotencyService = idempotencyService;
        this.paymentCacheService = paymentCacheService;
        this.currentUserService = currentUserService;
        this.bankAccountRepository = bankAccountRepository;
        this.transactionService = transactionService;
    }


    @Transactional
    @Override
    public TransferMoneyResponse transferMoney(
            TransferMoneyRequest request,
            String idempotencyKey) {

        UUID loggedInUserId =
                currentUserService.extractUserIdFromAuthentication();

        String hash;

        try {
            hash = requestHashService.generateTransferHash(
                    request,
                    loggedInUserId
            );
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        IdempotencyCheckResult idempotencyCheckResult =
                idempotencyService.check(
                        idempotencyKey,
                        hash
                );

        if (idempotencyCheckResult.getStatus()
                == IdempotencyCheckStatus.RETRY) {
            throw new IllegalStateException("Transfer idempotency replay is not implemented yet");
        }


        if (idempotencyCheckResult.getStatus()
                == IdempotencyCheckStatus.CONFLICT) {
            throw new IdempotencyConflictException("Idempotency key has already been used with a different request");
        }

        BankAccount sourceAccount =
                bankAccountRepository
                        .findByBankAccountNumber(
                                request.getSourceBankAccountNumber()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Source bank account not found")
                        );

        BankAccount destinationAccount =
                bankAccountRepository
                        .findByBankAccountNumber(
                                request.getDestinationBankAccountNumber()
                        )
                        .orElseThrow(() -> new ResourceNotFoundException("Destination bank account not found"));

        if (!sourceAccount.getUserId()
                .equals(loggedInUserId)) {

            throw new AccessDeniedException("You do not have access to the source bank account");
        }

        if (sourceAccount.getId().equals(destinationAccount.getId())) {
            throw new IllegalArgumentException("Source and destination bank accounts cannot be the same");
        }

        if (sourceAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Source bank account is not active");
        }

        if (destinationAccount.getStatus() != BankAccountStatus.ACTIVE) {
            throw new IllegalStateException("Destination bank account is not active");
        }

        if (!sourceAccount.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException("Transfer currency does not match "+ "source account currency");
        }

        if (!destinationAccount.getCurrency().equalsIgnoreCase(request.getCurrency())) {
            throw new IllegalArgumentException("Transfer currency does not match "+ "destination account currency");
        }

        BigDecimal amount = request.getAmount();

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException(
                    "Insufficient funds"
            );
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        sourceAccount.setUpdatedAt(LocalDateTime.now());

        destinationAccount.setBalance(destinationAccount.getBalance().add(amount));
        destinationAccount.setUpdatedAt(LocalDateTime.now());

        bankAccountRepository.save(sourceAccount);

        bankAccountRepository.save(destinationAccount);

        TransactionResponse transaction =
                transactionService.createTransferTransaction(
                        sourceAccount.getBankAccountNumber(),
                        destinationAccount.getBankAccountNumber(),
                        amount
                );

        return new TransferMoneyResponse(
                transaction.getTransactionId(),
                sourceAccount.getBankAccountNumber(),
                destinationAccount.getBankAccountNumber(),
                amount,
                request.getCurrency(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

    @Override
    public List<PaymentResponse> getAllPayments() {

        List<Payment> payments =
                paymentRepository.findAll();

        return payments.stream()
                .map(paymentMapper::toResponse)
                .toList();
    }

    @Override
    public PaymentResponse getPaymentByPaymentId(UUID paymentId) {

        PaymentResponse cachedPayment = paymentCacheService.get(paymentId);

        if (cachedPayment != null) {
            return cachedPayment;
        }

        Optional<Payment> payment = paymentRepository.findById(paymentId);

        if (payment.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        PaymentResponse response = paymentMapper.toResponse(payment.get());

        paymentCacheService.put(paymentId, response);

        return response;
    }

    @Override
    public PaymentResponse refundPayment(UUID paymentId) {

        Optional<Payment> paymentOptional = paymentRepository.findById(paymentId);

        if (paymentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        Payment payment = paymentOptional.get();

        UUID authenticatedUserId = currentUserService.extractUserIdFromAuthentication();

        if (!payment.getCustomerId()
                .equals(authenticatedUserId)) {

            throw new AccessDeniedException("You are not allowed to refund this payment");
        }

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new InvalidPaymentStatusTransitionException(
                    "Payment cannot be refunded from status "
                            + payment.getStatus()
            );
        }

        payment.setStatus(PaymentStatus.REFUND_PENDING);

        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        paymentCacheService.evict(paymentId);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse processRefund(
            UUID paymentId,
            UpdatePaymentStatusRequest request) {

        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);

        if (optionalPayment.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        Payment payment = optionalPayment.get();

        validateStatusTransition(payment.getStatus(), request);

        payment.setStatus(request.getStatus());

        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        paymentCacheService.evict(paymentId);

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse updatePaymentStatusByPaymentId(
            UUID paymentId,
            UpdatePaymentStatusRequest request) {

        Optional<Payment> payment = paymentRepository.findById(paymentId);

        if (payment.isEmpty()) {
            throw new ResourceNotFoundException("Payment not found with payment id " + paymentId);
        }

        validateStatusTransition(payment.get().getStatus(),request);

        payment.get().setStatus(request.getStatus());

        payment.get().setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment.get()        );

        paymentCacheService.evict(paymentId);

        return paymentMapper.toResponse(
                payment.get()
        );
    }

    private void validateStatusTransition(
            PaymentStatus currentStatus,
            UpdatePaymentStatusRequest request) {

        boolean valid = switch (currentStatus) {

            case CREATED ->
                    request.getStatus()
                            == PaymentStatus.PROCESSING;

            case PROCESSING ->
                    request.getStatus()
                            == PaymentStatus.SUCCESS
                            ||
                            request.getStatus()
                                    == PaymentStatus.FAILED;

            case SUCCESS ->
                    request.getStatus()
                            == PaymentStatus.REFUND_PENDING;

            case REFUND_PENDING ->
                    request.getStatus()
                            == PaymentStatus.REFUNDED
                            ||
                            request.getStatus()
                                    == PaymentStatus.REFUND_REJECTED;

            case FAILED,
                 REFUNDED,
                 REFUND_REJECTED ->
                    false;
        };

        if (!valid) {
            throw new InvalidPaymentStatusTransitionException("Invalid payment status transition from " + currentStatus + " to " + request.getStatus());
        }
    }
}