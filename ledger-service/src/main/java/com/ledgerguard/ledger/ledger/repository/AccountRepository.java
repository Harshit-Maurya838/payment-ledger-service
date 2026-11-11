package com.ledgerguard.ledger.ledger.repository;

import com.ledgerguard.ledger.ledger.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByName(String name);
}
