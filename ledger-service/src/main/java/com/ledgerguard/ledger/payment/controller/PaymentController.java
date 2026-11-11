package com.ledgerguard.ledger.payment.controller;

import com.ledgerguard.ledger.idempotency.IdempotentResponse;
import com.ledgerguard.ledger.idempotency.service.IdempotencyService;
import com.ledgerguard.ledger.ledger.LedgerEntry;
import com.ledgerguard.ledger.payment.CreatePaymentRequest;
import com.ledgerguard.ledger.payment.service.PaymentFlowService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final IdempotencyService idempotencyService;
    private final PaymentFlowService paymentFlowService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping
    public ResponseEntity<String> create(@RequestHeader("Idempotency-Key") String idemKey,
                                         @RequestBody String rawBody) {
        CreatePaymentRequest request = objectMapper.readValue(rawBody, CreatePaymentRequest.class);

        Set<ConstraintViolation<CreatePaymentRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        IdempotentResponse result = idempotencyService.execute(
                request.merchantId(), idemKey, rawBody,
                () -> objectMapper.writeValueAsString(paymentFlowService.process(request, idemKey)));

        return ResponseEntity
                .status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.body());
    }
}
