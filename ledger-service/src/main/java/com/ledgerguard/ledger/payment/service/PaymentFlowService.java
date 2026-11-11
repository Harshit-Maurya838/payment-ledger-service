package com.ledgerguard.ledger.payment.service;

import com.ledgerguard.ledger.payment.CreatePaymentRequest;
import com.ledgerguard.ledger.payment.Payment;
import com.ledgerguard.ledger.payment.PaymentResponse;
import com.ledgerguard.ledger.payment.PaymentStatus;
import com.ledgerguard.ledger.payment.psp.PspClient;
import com.ledgerguard.ledger.payment.psp.PspResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentFlowService {

    private final PaymentService paymentService;
    private final PspClient pspClient;

    // Deliberately NOT @Transactional: CREATED and PENDING must be committed
    // before the provider is called.
    public PaymentResponse process(CreatePaymentRequest request, String idemKey) {
        Payment payment = paymentService.create(
                request.merchantId(), request.amount(), request.currency(), idemKey);
        UUID id = payment.getId();

        paymentService.transition(id, PaymentStatus.PENDING);

        PspResult result = pspClient.charge(id, request.amount(), idemKey);

        Payment finalState = (result == PspResult.SUCCESS)
                ? paymentService.capture(id)
                : paymentService.transition(id, PaymentStatus.FAILED);

        return PaymentResponse.from(finalState);
    }

    // Not @Transactional: REFUND_PENDING commits before the provider is called.
    public PaymentResponse refund(UUID paymentId, String idemKey) {
        Payment payment = paymentService.transition(paymentId, PaymentStatus.REFUND_PENDING);

        PspResult result = pspClient.refund(paymentId, payment.getAmount(), idemKey);

        // On provider FAILURE the payment stays REFUND_PENDING and no ledger entries are posted.
        Payment finalState = (result == PspResult.SUCCESS)
                ? paymentService.completeRefund(paymentId)
                : payment;

        return PaymentResponse.from(finalState);
    }
}
