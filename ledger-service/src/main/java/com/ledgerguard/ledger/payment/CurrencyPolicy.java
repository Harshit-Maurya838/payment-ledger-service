package com.ledgerguard.ledger.payment;

import org.springframework.stereotype.Component;

@Component
public class CurrencyPolicy {

    public static final String DEFAULT_CURRENCY = "INR";

    // Skeleton: all amounts are INR for now, so nothing is enforced.
    // Extend here later (supported-currency list, match against account currency).
    public void validate(String currency) {
        // intentionally empty
    }
}