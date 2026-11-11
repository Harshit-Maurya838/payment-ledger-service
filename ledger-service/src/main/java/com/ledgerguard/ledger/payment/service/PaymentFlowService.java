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
}
