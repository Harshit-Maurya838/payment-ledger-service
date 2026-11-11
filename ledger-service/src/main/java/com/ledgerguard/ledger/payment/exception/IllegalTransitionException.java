package com.ledgerguard.ledger.payment.exception;

import com.ledgerguard.ledger.payment.PaymentStatus;

import java.util.UUID;

public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(UUID paymentId, PaymentStatus from, PaymentStatus to) {
        super("Illegal transition for payment " + paymentId + ": " + from + " -> " + to);
    }
}
