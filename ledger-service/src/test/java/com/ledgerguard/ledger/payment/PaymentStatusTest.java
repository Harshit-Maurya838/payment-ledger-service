package com.ledgerguard.ledger.payment;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.ledgerguard.ledger.payment.PaymentStatus.*;
import static org.assertj.core.api.Assertions.assertThat;

class PaymentStatusTest {

    private static final Map<PaymentStatus, Set<PaymentStatus>> ALLOWED = Map.of(
            CREATED, EnumSet.of(PENDING),
            PENDING, EnumSet.of(CAPTURED, FAILED),
            CAPTURED, EnumSet.of(REFUND_PENDING),
            REFUND_PENDING, EnumSet.of(REFUNDED),
            FAILED, EnumSet.noneOf(PaymentStatus.class),
            REFUNDED, EnumSet.noneOf(PaymentStatus.class));

    @Test
    void canMoveToMatchesTheAllowedTableForAll36Pairs() {
        int checked = 0;
        for (PaymentStatus from : values()) {
            for (PaymentStatus to : values()) {
                boolean expected = ALLOWED.get(from).contains(to);
                assertThat(from.canMoveTo(to))
                        .as("%s -> %s", from, to)
                        .isEqualTo(expected);
                checked++;
            }
        }
        assertThat(checked).isEqualTo(36);
    }
}
