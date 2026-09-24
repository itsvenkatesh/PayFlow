package org.venky.payflow.payment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.venky.payflow.common.exception.IdempotencyConflictException;
import org.venky.payflow.idempotency.service.IdempotencyCheckResult;
import org.venky.payflow.idempotency.service.IdempotencyCheckStatus;
import org.venky.payflow.idempotency.service.IdempotencyService;
import org.venky.payflow.idempotency.service.RequestHashService;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.entity.Payment;
import org.venky.payflow.payment.enums.PaymentStatus;
import org.venky.payflow.payment.mapper.PaymentMapper;
import org.venky.payflow.payment.repository.PaymentRepository;
import org.venky.payflow.user.security.AuthenticatedUser;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {
    private UUID customerId;

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RequestHashService requestHashService;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(
                        customerId,
                        "test@example.com",
                        List.of()
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.authorities()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }

    @Test
    void createPaymentRequest_whenIdempotencyKeyIsNew() throws NoSuchAlgorithmException {
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = "test-key-123";
        String requestHash = "test-hash";

        when(requestHashService.generateHash(request, customerId))
                .thenReturn(requestHash);

        when(idempotencyService.check(idempotencyKey, requestHash))
                .thenReturn(
                        new IdempotencyCheckResult(
                                IdempotencyCheckStatus.NEW,
                                null
                        )
                );

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());

        when(paymentMapper.toEntity(request))
                .thenReturn(payment);

        UUID paymentId = UUID.randomUUID();
        payment.setId(paymentId);

        when(paymentRepository.save(payment))
                .thenReturn(payment);

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(paymentId);

        when(paymentMapper.toResponse(payment))
                .thenReturn(paymentResponse);
        // Act
        PaymentResponse result = paymentService.createPaymentRequest(
                                    request,
                                    idempotencyKey
                                );
        // Assert - returned response
        assertEquals(paymentId, result.getId());
        // Capture the Payment that the service passed to the repository
        ArgumentCaptor<Payment> paymentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();

        // Assert - Payment created by the service
        assertEquals(paymentId, savedPayment.getId());
        assertEquals(1000L, savedPayment.getAmount());
        assertEquals(customerId, savedPayment.getCustomerId());
        assertEquals("INR", savedPayment.getCurrency());
        assertEquals(PaymentStatus.CREATED, savedPayment.getStatus());


        // Verify idempotency record was saved
        verify(idempotencyService).saveRecord(
                idempotencyKey,
                requestHash,
                paymentId
        );
    }

    @Test
    void createPaymentRequest_whenIdempotencyKeyIsRetried() throws NoSuchAlgorithmException {
        CreatePaymentRequest request = new CreatePaymentRequest();

        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = "test-key-123";
        String requestHash = "test-hash";

        UUID paymentId = UUID.randomUUID();
        Payment payment = new Payment();

        payment.setId(paymentId);
        payment.setAmount(request.getAmount());
        payment.setCustomerId(customerId);
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.CREATED);


        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));

        PaymentResponse paymentResponse = new PaymentResponse();
        paymentResponse.setId(paymentId);


        when(requestHashService.generateHash(request, customerId))
        .thenReturn(requestHash);

        when(idempotencyService.check(idempotencyKey, requestHash))
                .thenReturn(
                        new IdempotencyCheckResult(
                                IdempotencyCheckStatus.RETRY,
                                paymentId
                        )
                );

        when(paymentMapper.toResponse(payment))
        .thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPaymentRequest(
                request,
                idempotencyKey
        );

        // Assert
        assertEquals(paymentId, result.getId());
        verify(paymentRepository).findById(paymentId);

        verify(paymentMapper).toResponse(payment);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(idempotencyService, never())
                .saveRecord(any(), any(), any());
    }

    @Test
    void createPaymentRequest_whenIdempotencyKeyConflits() throws NoSuchAlgorithmException {
        CreatePaymentRequest request = new CreatePaymentRequest();

        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = "test-key-123";
        String requestHash = "test-hash";

        when(requestHashService.generateHash(request, customerId))
                .thenReturn(requestHash);

        when (idempotencyService.check(idempotencyKey, requestHash))
                .thenReturn(
                        new IdempotencyCheckResult(
                                IdempotencyCheckStatus.CONFLICT,
                                null
                        ));


        assertThrows(
                IdempotencyConflictException.class,
                () -> paymentService.createPaymentRequest(
                        request,
                        idempotencyKey
                )
        );

        verify(requestHashService)
                .generateHash(request, customerId);

        verify(idempotencyService)
                .check(idempotencyKey, requestHash);

        verify(paymentRepository, never())
                .save(any(Payment.class));

        verify(idempotencyService, never()).saveRecord(any(), any(), any());

    }
}