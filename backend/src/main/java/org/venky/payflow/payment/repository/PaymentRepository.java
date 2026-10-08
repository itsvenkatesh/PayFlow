package org.venky.payflow.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.venky.payflow.payment.entity.Payment;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
