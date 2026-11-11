package com.ledgerguard.ledger.payment.controller;

import com.ledgerguard.ledger.idempotency.IdempotentResponse;
import com.ledgerguard.ledger.idempotency.service.IdempotencyService;
import com.ledgerguard.ledger.payment.CreatePaymentRequest;
import com.ledgerguard.ledger.payment.Payment;
import com.ledgerguard.ledger.payment.service.PaymentFlowService;
import com.ledgerguard.ledger.payment.service.PaymentService;
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
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final IdempotencyService idempotencyService;
    private final PaymentFlowService paymentFlowService;
    private final PaymentService paymentService;
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

        return toResponse(result);
    }

    @PostMapping("/{id}/refunds")
    public ResponseEntity<String> refund(@PathVariable UUID id,
                                         @RequestHeader("Idempotency-Key") String idemKey,
                                         @RequestBody(required = false) String rawBody) {
        Payment payment = paymentService.get(id); // 404 before any idempotency row is created

        // The body is empty for a full refund, so the target payment id is part of the hash input.
        String fingerprint = id + ":" + (rawBody == null ? "" : rawBody);

        IdempotentResponse result = idempotencyService.execute(
                payment.getMerchantId(), idemKey, fingerprint,
                () -> objectMapper.writeValueAsString(paymentFlowService.refund(id, idemKey)));

        return toResponse(result);
    }

    private ResponseEntity<String> toResponse(IdempotentResponse result) {
        return ResponseEntity
                .status(result.replayed() ? HttpStatus.OK : HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.body());
    }
}