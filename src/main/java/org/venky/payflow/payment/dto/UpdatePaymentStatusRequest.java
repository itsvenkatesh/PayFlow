package org.venky.payflow.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.venky.payflow.payment.enums.PaymentStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePaymentStatusRequest {
    private PaymentStatus status;
}
