package com.ledgerguard.ledger.ledger.repository;

import com.ledgerguard.ledger.ledger.LedgerTransaction;
import com.ledgerguard.ledger.ledger.LedgerTransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, UUID> {
    LedgerTransactionType id(UUID id);
}