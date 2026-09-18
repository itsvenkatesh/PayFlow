package org.venky.payflow.idempotency.service;

import org.springframework.stereotype.Service;
import org.venky.payflow.idempotency.entity.IdempotencyRecord;
import org.venky.payflow.idempotency.repository.IdempotencyRecordRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public IdempotencyServiceImpl(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    @Override
    public IdempotencyCheckResult check(String idempotencyKey, String requestHash) {

        Optional<IdempotencyRecord> record = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);

        if  (record.isEmpty()) {
            return new IdempotencyCheckResult(
                    IdempotencyCheckStatus.NEW,
            null
            );
        }

        IdempotencyRecord existingRecord = record.get();

        if (existingRecord.getRequestHash().equals(requestHash)){
            return new IdempotencyCheckResult(
                    IdempotencyCheckStatus.RETRY,
                    existingRecord.getPaymentId()
            );
        }
        return new IdempotencyCheckResult(
                IdempotencyCheckStatus.CONFLICT,
                null
            );
    }

    @Override
    public IdempotencyRecord saveRecord( String idempotencyKey,String hash , UUID paymentId) {
        IdempotencyRecord idempotencyRecord = new IdempotencyRecord(idempotencyKey, hash, paymentId);
        return idempotencyRecordRepository.save(idempotencyRecord);
    }
}
