package com.ledgerguard.ledger.payment;

import java.util.UUID;

public record PaymentResponse(UUID id, UUID merchantId, long amount, String currency, PaymentStatus status) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getMerchantId(), p.getAmount(), p.getCurrency(), p.getStatus());
    }
}
