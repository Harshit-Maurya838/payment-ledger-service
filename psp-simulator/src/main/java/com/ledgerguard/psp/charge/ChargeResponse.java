package com.ledgerguard.psp.charge;

public record ChargeResponse(String chargeId, ChargeStatus status) {

    public static ChargeResponse from(Charge charge) {
        return new ChargeResponse(charge.chargeId(), charge.status());
    }
}