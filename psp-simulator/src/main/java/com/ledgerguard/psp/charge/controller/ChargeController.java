package com.ledgerguard.psp.charge.controller;

import com.ledgerguard.psp.charge.Charge;
import com.ledgerguard.psp.charge.ChargeResponse;
import com.ledgerguard.psp.charge.CreateChargeRequest;
import com.ledgerguard.psp.charge.service.ChargeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/charges")
@RequiredArgsConstructor
public class ChargeController {

    private final ChargeService chargeService;

    @PostMapping
    public ChargeResponse create(@RequestHeader("Idempotency-Key") String idemKey,
                                 @Valid @RequestBody CreateChargeRequest request) {
        return ChargeResponse.from(chargeService.charge(idemKey, request));
    }

    @GetMapping("/{id}")
    public Charge get(@PathVariable String id) {
        return chargeService.get(id);
    }
}