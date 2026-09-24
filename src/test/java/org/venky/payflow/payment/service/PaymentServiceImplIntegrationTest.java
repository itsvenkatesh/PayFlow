package org.venky.payflow.payment.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.venky.payflow.common.exception.IdempotencyConflictException;
import org.venky.payflow.idempotency.repository.IdempotencyRecordRepository;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.entity.Payment;
import org.venky.payflow.payment.enums.PaymentStatus;
import org.venky.payflow.payment.repository.PaymentRepository;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.idempotency.entity.IdempotencyRecord;
import org.venky.payflow.user.security.AuthenticatedUser;


import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceImplIntegrationTest {
    private UUID customerId;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("payflow_test")
                    .withUsername("test")
                    .withPassword("test");

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IdempotencyRecordRepository idempotencyRepository;

    @BeforeEach
    void cleanDatabase() {
        idempotencyRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    @BeforeEach
    void setUpSecurityContext() {

        customerId = UUID.randomUUID();

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(
                        customerId,
                        "test@example.com",
                        List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.authorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createPayment_shouldPersistPaymentAndIdempotencyRecord() {

        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = UUID.randomUUID().toString();


        // Act
        PaymentResponse response =
                paymentService.createPaymentRequest(request, idempotencyKey);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId());

        Payment savedPayment =
                paymentRepository.findById(response.getId()).orElseThrow();

        assertEquals(1000L, savedPayment.getAmount());
        assertEquals("INR", savedPayment.getCurrency());
        assertEquals(PaymentStatus.CREATED, savedPayment.getStatus());

        IdempotencyRecord record =
                idempotencyRepository
                        .findByIdempotencyKey(idempotencyKey)
                        .orElseThrow();

        assertEquals(response.getId(), record.getPaymentId());
        assertEquals(idempotencyKey, record.getIdempotencyKey());
    }

    @Test
    void createPayment_withSameIdempotencyKey_shouldReturnSamePayment() {

        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = UUID.randomUUID().toString();

        // Act
        PaymentResponse firstResponse =
                paymentService.createPaymentRequest(request, idempotencyKey);

        PaymentResponse secondResponse =
                paymentService.createPaymentRequest(request, idempotencyKey);

        // Assert
        assertEquals(firstResponse.getId(), secondResponse.getId());

        assertEquals(1, paymentRepository.count());
        assertEquals(1, idempotencyRepository.count());
    }

    @Test
    void createPayment_withDifferentIdempotencyKeyAndSameRequest_shouldCreateNewPayment() {

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String firstIdempotencyKey = UUID.randomUUID().toString();
        String secondIdempotencyKey = UUID.randomUUID().toString();

        PaymentResponse firstResponse =
                paymentService.createPaymentRequest(
                        request,
                        firstIdempotencyKey
                );

        PaymentResponse secondResponse =
                paymentService.createPaymentRequest(
                        request,
                        secondIdempotencyKey
                );

        assertNotEquals(
                firstResponse.getId(),
                secondResponse.getId()
        );

        assertEquals(2, paymentRepository.count());
        assertEquals(2, idempotencyRepository.count());
    }


    @Test
    void createPayment_withSameIdempotencyKeyAndDifferentRequest_shouldThrowConflict() {

        // Arrange
        CreatePaymentRequest firstRequest = new CreatePaymentRequest();
        firstRequest.setAmount(1000L);
        firstRequest.setCurrency("INR");

        CreatePaymentRequest secondRequest = new CreatePaymentRequest();
        secondRequest.setAmount(2000L);
        secondRequest.setCurrency("INR");

        String idempotencyKey = UUID.randomUUID().toString();

        // Act
        paymentService.createPaymentRequest(
                firstRequest,
                idempotencyKey
        );

        // Assert
        assertThrows(
                IdempotencyConflictException.class,
                () -> paymentService.createPaymentRequest(
                        secondRequest,
                        idempotencyKey
                )
        );

        assertEquals(1, paymentRepository.count());
        assertEquals(1, idempotencyRepository.count());
    }

    @Test
    void createPayment_whenIdempotencyRecordSaveFails_shouldRollbackPayment() {

        // Arrange
        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = UUID.randomUUID().toString();

        // Create an existing idempotency record with the same key
        UUID existingPaymentId = UUID.randomUUID();

        IdempotencyRecord existingRecord = new IdempotencyRecord(
                idempotencyKey,
                "some-existing-hash",
                existingPaymentId
        );

        idempotencyRepository.save(existingRecord);

        // Act + Assert
        assertThrows(
                Exception.class,
                () -> paymentService.createPaymentRequest(
                        request,
                        idempotencyKey
                )
        );

        // Assert
        assertEquals(0, paymentRepository.count());
    }

    private void setSecurityContext() {

        AuthenticatedUser authenticatedUser =
                new AuthenticatedUser(
                        customerId,
                        "test@example.com",
                        List.of(
                                new SimpleGrantedAuthority("ROLE_CUSTOMER")
                        )
                );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authenticatedUser,
                        null,
                        authenticatedUser.authorities()
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
    }


    @Test
    void createPayment_withConcurrentSameIdempotencyKey_shouldNotCreateDuplicatePayments()
            throws Exception {

        CreatePaymentRequest request = new CreatePaymentRequest();
        request.setAmount(1000L);
        request.setCurrency("INR");

        String idempotencyKey = UUID.randomUUID().toString();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<PaymentResponse> task = () -> {

            setSecurityContext();

            try {
                return paymentService.createPaymentRequest(
                        request,
                        idempotencyKey
                );
            } finally {
                SecurityContextHolder.clearContext();
            }
        };

        Future<PaymentResponse> first = executor.submit(task);
        Future<PaymentResponse> second = executor.submit(task);

        try {
            PaymentResponse firstResponse = first.get();
            System.out.println(
                    "FIRST SUCCESS: " + firstResponse.getId()
            );
        } catch (Exception e) {
            System.out.println("FIRST FAILED:");
            e.printStackTrace();
        }

        try {
            PaymentResponse secondResponse = second.get();
            System.out.println(
                    "SECOND SUCCESS: " + secondResponse.getId()
            );
        } catch (Exception e) {
            System.out.println("SECOND FAILED:");
            e.printStackTrace();
        }

        executor.shutdown();

        System.out.println(
                "PAYMENT COUNT = " + paymentRepository.count()
        );

        System.out.println(
                "IDEMPOTENCY COUNT = " + idempotencyRepository.count()
        );
    }
}