package com.ledgerguard.ledger.ledger;

import java.util.UUID;

public record Entry(UUID accountId, long amount) {
}
