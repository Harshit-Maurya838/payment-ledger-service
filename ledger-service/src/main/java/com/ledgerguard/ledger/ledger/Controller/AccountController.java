package com.ledgerguard.ledger.ledger.Controller;

import com.ledgerguard.ledger.ledger.AccountBalance;
import com.ledgerguard.ledger.ledger.Services.LedgerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final LedgerService ledgerService;

    @GetMapping("/{id}/balance")
    public AccountBalance balance(@PathVariable UUID id) {
        return ledgerService.getBalance(id);
    }
}
