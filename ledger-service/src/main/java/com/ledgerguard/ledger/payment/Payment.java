package com.ledgerguard.ledger.payment;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID merchantId;
    private long amount;
    private String currency;

    @Enumerated(EnumType.STRING)
    @Setter
    private PaymentStatus status;

    @Setter
    private String providerRef;

    private String idemKey;

    @Version
    private long version;

    public Payment(UUID merchantId, long amount, String currency, String idemKey) {
        this.merchantId = merchantId;
        this.amount = amount;
        this.currency = currency;
        this.idemKey = idemKey;
        this.status = PaymentStatus.CREATED;
    }
}
