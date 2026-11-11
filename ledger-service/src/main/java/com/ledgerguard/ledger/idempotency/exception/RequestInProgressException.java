package com.ledgerguard.ledger.idempotency.exception;

public class RequestInProgressException extends RuntimeException {
    public RequestInProgressException(String idemKey) {
        super("A request with this idempotency key is still in progress: " + idemKey);
    }
}
