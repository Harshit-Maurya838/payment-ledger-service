package com.ledgerguard.ledger.idempotency.exception;

public class IdempotencyKeyMismatchException extends RuntimeException {
    public IdempotencyKeyMismatchException(String idemKey) {
        super("Idempotency key reused with a different request body: " + idemKey);
    }
}
