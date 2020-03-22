package com.network.shopping.web.rest;

import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public Page<AccountDTO> getAllAccounts(Pageable pageable) {
        return accountService.getAllAccounts(pageable);
    }

    @GetMapping(value = "/{number}")
    public ResponseEntity getAllAccounts(@PathVariable(name = "number") String number) {
        log.debug("Request to retrieve account by number= {}", number);
        return ResponseEntity.ok(accountService.getUserAccountByNumber(number));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountDTO createAccount(@Valid @NotNull @RequestBody AccountDTO account) {
        log.debug("Request to create new account {} ", account);
        return accountService.addAccount(account);
    }

    @PostMapping("/{accountId}/beneficiary")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO addBeneficiaryToAccount(@PathVariable String accountId,
                                              @Valid @NotEmpty(message = "Input beneficiaries list cannot be empty.")
                                              @RequestBody List<@Valid BeneficiaryDTO> beneficiaryDTOS) {
        log.debug("Request to add {} beneficiaries to account number {} ", beneficiaryDTOS.size(), accountId);
        return accountService.addBeneficiariesToAccount(accountId, beneficiaryDTOS);
    }

    @PostMapping("/{accountId}/card")
    @ResponseStatus(HttpStatus.OK)
    public AccountDTO addCreditCardToAccount(@PathVariable String accountId,
                                             @NotBlank(message = "credit card number is mandatory")
                                             @RequestBody String cardNumber) {
        log.debug("Request to add a credit card number={} to account number {} ", cardNumber, accountId);
        return accountService.addCreditCardToAccount(accountId, cardNumber);
    }
}
