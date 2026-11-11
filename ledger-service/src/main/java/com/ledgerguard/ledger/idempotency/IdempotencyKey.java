package com.ledgerguard.ledger.idempotency;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "idempotency_keys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdempotencyKey {

    @EmbeddedId
    private IdempotencyKeyId id;

    private String requestHash;
    private String responseBody;

    @Enumerated(EnumType.STRING)
    private IdempotencyStatus status;
}
