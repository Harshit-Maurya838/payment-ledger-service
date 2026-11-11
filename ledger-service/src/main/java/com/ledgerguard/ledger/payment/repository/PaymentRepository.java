package com.ledgerguard.ledger.payment.repository;

import com.ledgerguard.ledger.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
