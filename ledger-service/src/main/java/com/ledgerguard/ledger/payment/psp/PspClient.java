package com.ledgerguard.ledger.payment.psp;

import java.util.UUID;

public interface PspClient {
    PspResult charge(UUID paymentId, long amount, String idemKey);
}
