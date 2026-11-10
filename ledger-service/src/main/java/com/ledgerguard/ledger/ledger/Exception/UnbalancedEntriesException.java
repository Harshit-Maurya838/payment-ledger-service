package com.ledgerguard.ledger.ledger.Exception;

public class UnbalancedEntriesException extends RuntimeException {
    public UnbalancedEntriesException(String message) {
        super(message);
    }
}