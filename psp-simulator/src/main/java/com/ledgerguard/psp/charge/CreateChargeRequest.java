package com.ledgerguard.psp.charge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateChargeRequest(
        @NotNull UUID paymentId,
        @Positive long amount,
        @NotBlank @Size(min = 3, max = 3) String currency) {}