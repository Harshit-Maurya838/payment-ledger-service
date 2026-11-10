package com.ledgerguard.ledger.ledger.Services;

import com.ledgerguard.ledger.ledger.*;
import com.ledgerguard.ledger.ledger.Exception.AccountNotFoundException;
import com.ledgerguard.ledger.ledger.Exception.UnbalancedEntriesException;
import com.ledgerguard.ledger.ledger.Repository.AccountRepository;
import com.ledgerguard.ledger.ledger.Repository.LedgerEntryRepository;
import com.ledgerguard.ledger.ledger.Repository.LedgerTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public LedgerTransaction post(LedgerTransactionType type, UUID paymentId, List<Entry> entries) {
        if(entries == null || entries.size() < 2){
            throw new UnbalancedEntriesException("A ledger transaction needs at least 2 entries");
        }

        long sum = entries.stream().mapToLong(Entry::amount).sum();
        if(sum != 0){
            throw new UnbalancedEntriesException("Entries must sum to 0 but sum to " + sum);
        }
        LedgerTransaction tx = transactionRepository.save(new LedgerTransaction(paymentId, type));
        entryRepository.saveAll(entries.stream()
                .map(e -> new LedgerEntry(tx.getId(), e.accountId(), e.amount()))
                .toList());
        return tx;
    }

    @Transactional(readOnly = true)
    public AccountBalance getBalance(UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        return new AccountBalance(accountId, entryRepository.balanceOf(accountId), account.getCurrency());
    }
}
