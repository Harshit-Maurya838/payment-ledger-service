package com.ledgerguard.ledger.idempotency;

public record IdempotentResponse(String body, boolean replayed) {
}
