package com.ledgerguard.ledger.ledger;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "ledger_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    private LedgerTransactionType type;

    public LedgerTransaction(UUID paymentId, LedgerTransactionType type) {
        this.paymentId = paymentId;
        this.type = type;
    }
}
