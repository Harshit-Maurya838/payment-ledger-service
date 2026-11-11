package com.ledgerguard.ledger.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreatePaymentRequest(
        @NotNull UUID merchantId,
        @NotNull long amount,
        @NotBlank @Size(min = 3, max = 3) String currency) {
}
