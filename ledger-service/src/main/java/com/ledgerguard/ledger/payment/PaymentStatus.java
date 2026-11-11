package com.ledgerguard.ledger.payment;

public enum PaymentStatus {

    CREATED, PENDING, CAPTURED, FAILED, REFUND_PENDING, REFUNDED;

    public boolean canMoveTo(PaymentStatus next) {
        return switch (this) {
            case CREATED        -> next == PENDING;
            case PENDING        -> next == CAPTURED || next == FAILED;
            case CAPTURED       -> next == REFUND_PENDING;
            case REFUND_PENDING -> next == REFUNDED;
            case FAILED, REFUNDED -> false;
        };
    }
}
