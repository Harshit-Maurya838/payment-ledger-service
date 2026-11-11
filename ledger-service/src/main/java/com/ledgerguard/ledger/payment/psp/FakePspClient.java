package com.ledgerguard.ledger.payment.psp;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class FakePspClient implements PspClient {

    private final double failureRate;

    public FakePspClient(@Value("${psp.fake.failure-rate:0.0}") double failureRate) {
        this.failureRate = failureRate;
    }

    @Override
    public PspResult charge(UUID paymentId, long amount, String idemKey) {
        return ThreadLocalRandom.current().nextDouble() < failureRate
                ? PspResult.FAILURE
                : PspResult.SUCCESS;
    }
}
