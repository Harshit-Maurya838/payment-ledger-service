package com.ledgerguard.psp.charge;

import java.time.Instant;
import java.util.UUID;

public record Charge(String chargeId, UUID paymentId, long amount, String currency, ChargeStatus status, Instant createdAt) {}