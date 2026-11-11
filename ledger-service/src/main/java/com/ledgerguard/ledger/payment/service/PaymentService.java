package com.ledgerguard.ledger.payment.service;

import com.ledgerguard.ledger.payment.Payment;
import com.ledgerguard.ledger.payment.PaymentStatus;
import com.ledgerguard.ledger.payment.exception.IllegalTransitionException;
import com.ledgerguard.ledger.payment.exception.PaymentNotFoundException;
import com.ledgerguard.ledger.payment.respository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;

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
}
