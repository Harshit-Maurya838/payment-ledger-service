package com.ledgerguard.ledger.ledger.exception;

public class UnbalancedEntriesException extends RuntimeException {
    public UnbalancedEntriesException(String message) {
        super(message);
    }
}