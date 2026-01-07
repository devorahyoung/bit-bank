package com.poalim.mybank.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransferRecordRepository extends JpaRepository<TransferRecord, Long> {
    Optional<TransferRecord> findByIdempotencyKey(String idempotencyKey);
}
