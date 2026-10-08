package org.venky.payflow.idempotency.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyCheckResult {
    private IdempotencyCheckStatus status;
    private UUID paymentId;
}
