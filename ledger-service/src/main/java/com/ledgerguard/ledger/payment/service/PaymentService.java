package com.ledgerguard.ledger.payment.service;

import com.ledgerguard.ledger.ledger.Entry;
import com.ledgerguard.ledger.ledger.LedgerTransactionType;
import com.ledgerguard.ledger.ledger.repository.AccountRepository;
import com.ledgerguard.ledger.ledger.service.LedgerService;
import com.ledgerguard.ledger.payment.Payment;
import com.ledgerguard.ledger.payment.PaymentStatus;
import com.ledgerguard.ledger.payment.exception.IllegalTransitionException;
import com.ledgerguard.ledger.payment.exception.PaymentNotFoundException;
import com.ledgerguard.ledger.payment.respository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LedgerService ledgerService;
    private final AccountRepository accountRepository;


    @Transactional(readOnly = true)
    public Payment get(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    @Transactional
    public Payment create(UUID merchantId, long amount, String currency, String idemKey) {
        return paymentRepository.save(new Payment(merchantId, amount, currency, idemKey));
    }

    public Payment transition(UUID paymentId, PaymentStatus next) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));
        PaymentStatus current = payment.getStatus();

        if(!current.canMoveTo(next)){
            log.warn("Rejected illegal transition for payment {}: {} -> {}", paymentId, current, next);
            throw new IllegalTransitionException(paymentId, current, next);
        }

        payment.setStatus(next);
        return paymentRepository.save(payment);
    }

    // State change and ledger entries commit together in one transaction.
    @Transactional
    public Payment capture(UUID paymentId) {
        Payment payment = transition(paymentId, PaymentStatus.CAPTURED);
        ledgerService.post(LedgerTransactionType.CAPTURE, paymentId, List.of(
                new Entry(accountId("customer_funds"), payment.getAmount()),       // debit
                new Entry(accountId("merchant_payable"), -payment.getAmount())));  // credit
        return payment;
    }

    @Transactional
    public Payment completeRefund(UUID paymentId) {
        Payment payment = transition(paymentId, PaymentStatus.REFUNDED);
        ledgerService.post(LedgerTransactionType.REFUND, paymentId, List.of(
                new Entry(accountId("customer_funds"), -payment.getAmount()),      // reverse the debit
                new Entry(accountId("merchant_payable"), payment.getAmount())));   // reverse the credit
        return payment;
    }

    private UUID accountId(String name) {
        return accountRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Seed account missing: " + name))
                .getId();
    }
}
