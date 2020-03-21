package com.network.shopping.web.rest;

import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/api/v1/accounts")
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
}
