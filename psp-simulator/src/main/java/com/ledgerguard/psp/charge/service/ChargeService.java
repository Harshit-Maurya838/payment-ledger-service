package com.ledgerguard.psp.charge.service;

import com.ledgerguard.psp.charge.Charge;
import com.ledgerguard.psp.charge.ChargeStatus;
import com.ledgerguard.psp.charge.CreateChargeRequest;
import com.ledgerguard.psp.charge.exception.ChargeNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChargeService {

    private final Map<String, Charge> chargesByKey = new ConcurrentHashMap<>();
    private final Map<String, Charge> chargesById = new ConcurrentHashMap<>();

    // computeIfAbsent is atomic per key: concurrent requests with the same
    // Idempotency-Key all get the same charge, and only one is ever created.
    public Charge charge(String idemKey, CreateChargeRequest request) {
        return chargesByKey.computeIfAbsent(idemKey, key -> {
            Charge charge = new Charge(
                    "ch_" + UUID.randomUUID(),
                    request.paymentId(),
                    request.amount(),
                    request.currency(),
                    ChargeStatus.SUCCEEDED,
                    Instant.now());
            chargesById.put(charge.chargeId(), charge);
            return charge;
        });
    }

    public Charge get(String chargeId) {
        Charge charge = chargesById.get(chargeId);
        if (charge == null) {
            throw new ChargeNotFoundException(chargeId);
        }
        return charge;
    }
}