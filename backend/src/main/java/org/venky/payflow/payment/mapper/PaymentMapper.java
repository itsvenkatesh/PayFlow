package org.venky.payflow.payment.mapper;

import org.mapstruct.Mapper;
import org.venky.payflow.payment.dto.CreatePaymentRequest;
import org.venky.payflow.payment.dto.PaymentResponse;
import org.venky.payflow.payment.entity.Payment;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    Payment toEntity(CreatePaymentRequest request);

    PaymentResponse toResponse(Payment payment);
}