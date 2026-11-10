package com.ledgerguard.ledger.ledger.Repository;

import com.ledgerguard.ledger.ledger.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("select coalesce(sum(e.amount), 0L) from LedgerEntry e where e.accountId = :accountId")
    long balanceOf(@Param("accountId") UUID accountId);
}