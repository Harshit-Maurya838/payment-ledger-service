package com.ledgerguard.ledger.idempotency;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record IdempotencyKeyId(UUID merchantId, String idemKey) implements Serializable {
}
