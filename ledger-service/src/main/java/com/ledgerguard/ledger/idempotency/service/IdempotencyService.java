package com.ledgerguard.ledger.idempotency.service;

import com.ledgerguard.ledger.idempotency.IdempotencyKey;
import com.ledgerguard.ledger.idempotency.IdempotencyKeyId;
import com.ledgerguard.ledger.idempotency.IdempotencyStatus;
import com.ledgerguard.ledger.idempotency.IdempotentResponse;
import com.ledgerguard.ledger.idempotency.exception.IdempotencyKeyMismatchException;
import com.ledgerguard.ledger.idempotency.exception.RequestInProgressException;
import com.ledgerguard.ledger.idempotency.repository.IdempotencyKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final IdempotencyKeyRepository repository;

    // Deliberately NOT @Transactional: the IN_PROGRESS row must commit on its own,
    // before the payment runs, so concurrent retries can see it.
    public IdempotentResponse execute(UUID merchantId, String idemKey, String requestBody, Supplier<String> action){
        String hash = sha256Hex(requestBody);

        if(repository.insertIfAbsent(merchantId, idemKey, hash) == 0){
            IdempotencyKey existing = repository.findById(new IdempotencyKeyId(merchantId, idemKey))
                    .orElseThrow(() -> new IllegalStateException("Idempotency key vanished: " + idemKey));

            if (!existing.getRequestHash().equals(hash)) {
                throw new IdempotencyKeyMismatchException(idemKey);
            }
            if (existing.getStatus() == IdempotencyStatus.IN_PROGRESS) {
                throw new RequestInProgressException(idemKey);
            }
            return new IdempotentResponse(existing.getResponseBody(), true);
        }

        // If action throws, the key stays IN_PROGRESS on purpose: we cannot know whether
        // the provider was charged, so retries get 409 instead of risking a second charge.
        String responseBody = action.get();
        repository.markCompleted(merchantId, idemKey, responseBody);
        return new IdempotentResponse(responseBody, false);
    }

    private static String sha256Hex(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
