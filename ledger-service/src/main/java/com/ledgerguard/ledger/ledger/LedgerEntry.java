package com.ledgerguard.ledger.ledger;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID transactionId;
    private UUID accountId;
    private long amount;

    public LedgerEntry(UUID transactionId, UUID accountId, long amount) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
    }
}
