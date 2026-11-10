package com.ledgerguard.ledger.ledger;

import java.util.UUID;

public record AccountBalance(UUID accountId, long balance, String currency) {}
