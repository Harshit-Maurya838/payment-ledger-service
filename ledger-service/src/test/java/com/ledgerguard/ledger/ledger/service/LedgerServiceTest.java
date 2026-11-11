package com.ledgerguard.ledger.ledger.service;

import com.ledgerguard.ledger.ledger.Entry;
import com.ledgerguard.ledger.ledger.LedgerTransaction;
import com.ledgerguard.ledger.ledger.LedgerTransactionType;
import com.ledgerguard.ledger.ledger.exception.UnbalancedEntriesException;
import com.ledgerguard.ledger.ledger.repository.AccountRepository;
import com.ledgerguard.ledger.ledger.repository.LedgerEntryRepository;
import com.ledgerguard.ledger.ledger.repository.LedgerTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class LedgerServiceTest {

    private LedgerTransactionRepository transactionRepository;
    private LedgerEntryRepository entryRepository;
    private AccountRepository accountRepository;
    private LedgerService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(LedgerTransactionRepository.class);
        entryRepository = mock(LedgerEntryRepository.class);
        accountRepository = mock(AccountRepository.class);
        service = new LedgerService(transactionRepository, entryRepository, accountRepository);
    }

    private void assertNoRepositoryTouched() {
        verifyNoInteractions(transactionRepository, entryRepository, accountRepository);
    }

    @Test
    void unbalancedEntriesAreRejected() {
        List<Entry> entries = List.of(new Entry(UUID.randomUUID(), 100), new Entry(UUID.randomUUID(), -90));

        assertThatThrownBy(() -> service.post(LedgerTransactionType.CAPTURE, UUID.randomUUID(), entries))
                .isInstanceOf(UnbalancedEntriesException.class);
        assertNoRepositoryTouched();
    }

    @Test
    void emptyEntryListIsRejected() {
        assertThatThrownBy(() -> service.post(LedgerTransactionType.CAPTURE, UUID.randomUUID(), List.of()))
                .isInstanceOf(UnbalancedEntriesException.class);
        assertNoRepositoryTouched();
    }

    @Test
    void singleEntryListIsRejectedEvenWithZeroAmount() {
        List<Entry> entries = List.of(new Entry(UUID.randomUUID(), 0));

        assertThatThrownBy(() -> service.post(LedgerTransactionType.CAPTURE, UUID.randomUUID(), entries))
                .isInstanceOf(UnbalancedEntriesException.class);
        assertNoRepositoryTouched();
    }

    @Test
    void balancedEntriesAreSaved() {
        UUID paymentId = UUID.randomUUID();
        List<Entry> entries = List.of(new Entry(UUID.randomUUID(), 500), new Entry(UUID.randomUUID(), -500));
        when(transactionRepository.save(any(LedgerTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        LedgerTransaction result = service.post(LedgerTransactionType.CAPTURE, paymentId, entries);

        verify(transactionRepository).save(any(LedgerTransaction.class));
        verify(entryRepository).saveAll(anyIterable());
        assertThat(result.getType()).isEqualTo(LedgerTransactionType.CAPTURE);
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
    }
}
