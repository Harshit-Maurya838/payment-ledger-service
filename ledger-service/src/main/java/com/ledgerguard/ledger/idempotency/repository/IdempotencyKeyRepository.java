package com.ledgerguard.ledger.idempotency.repository;

import com.ledgerguard.ledger.idempotency.IdempotencyKey;
import com.ledgerguard.ledger.idempotency.IdempotencyKeyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, IdempotencyKeyId> {

    @Transactional
    @Modifying
    @Query(value = """
            INSERT INTO idempotency_keys (merchant_id, idem_key, request_hash, status)
            VALUES (:merchantId, :idemKey, :requestHash, 'IN_PROGRESS')
            ON CONFLICT (merchant_id, idem_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("merchantId") UUID merchantId,
                       @Param("idemKey") String idemKey,
                       @Param("requestHash") String requestHash);

    @Transactional
    @Modifying
    @Query(value = """
            UPDATE idempotency_keys
            SET response_body = :responseBody, status = 'COMPLETED'
            WHERE merchant_id = :merchantId AND idem_key = :idemKey
            """, nativeQuery = true)
    int markCompleted(@Param("merchantId") UUID merchantId,
                      @Param("idemKey") String idemKey,
                      @Param("responseBody") String responseBody);
}
