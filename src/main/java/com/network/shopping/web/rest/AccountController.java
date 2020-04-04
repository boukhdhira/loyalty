package com.network.shopping.web.rest;

import com.network.shopping.service.AccountService;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import com.network.shopping.service.utils.RestRequestUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
//TODO: all service with account number on param must be verified if authenticaed user is the ower of this account
@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
@Api(value = "Account management endpoint")
public class AccountController {

    private final AccountService accountService;

    @Value("${application.name}")
    private String applicationName;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @ApiOperation(value = "retrieve list of all registered accounts", response = Page.class)
    public @ResponseBody
    Page<AccountDTO> accountSummary(Pageable pageable) {
        return this.accountService.getAllAccounts(pageable);
    }

    @GetMapping(value = "/{accountId}")
    @ApiOperation(value = "Getting account details by id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved account"),
            @ApiResponse(code = 404, message = "The account you were trying to reach is not found or id invalid")
    })
    public ResponseEntity retrieveAccountDetailsByNumber(@PathVariable(name = "accountId") String number) {
        log.debug("Request to retrieve account by number= {}", number);
        return ResponseEntity.ok(this.accountService.getUserAccountByNumber(number));
    }

//    @ApiOperation(value = "Add a new account")
//    @PostMapping
//    @ResponseStatus(HttpStatus.CREATED)
//    @ApiResponses(value = {
//            @ApiResponse(code = 201, message = "Successfully save a new account"),
//            @ApiResponse(code = 400, message = "Validation failed for account data"),
//            @ApiResponse(code = 409, message = "The account id or card number you were trying to save has been used")})
//    public ResponseEntity<Void> registerAccount(@Valid @NotNull @RequestBody
//                                                @ApiParam(value = "Account object to store database", required = true)
//                                                        AccountDTO account) {
//        log.debug("Request to create new account {} ", account);
//        this.accountService.createAccount(account);
//        return entityWithLocation(account.getNumber());
//    }

    @ApiOperation(value = "Attache a new beneficiary to an existing account")
    @PostMapping("/{accountId}/beneficiary")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully save a new beneficiary for given account"),
            @ApiResponse(code = 400, message = "Validation failed for beneficiary data or account id")})
    public AccountDTO addBeneficiaryToAccount(@PathVariable @ApiParam(value = "account identifier", required = true)
                                                      String accountId,
                                              @Valid @ApiParam(value = "list of account beneficiaries with there " +
                                                      "percentage", required = true) @NotEmpty(message = "Input beneficiaries list cannot be empty.")
                                              @RequestBody List<@Valid BeneficiaryDTO> beneficiaryDTOS) {
        log.debug("Request to add {} beneficiaries to account number {} ", beneficiaryDTOS.size(), accountId);
        return this.accountService.addBeneficiariesToAccount(accountId, beneficiaryDTOS);
    }

    /**
     * {@code POST /accounts/:accountId/card} : Attach credit card number
     * to an account by his account identifier.
     *
     * @param accountId  account number
     * @param cardNumber credit card number
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)}.
     */
    @PostMapping("/{accountId}/card")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "Add credit card number to existing account")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully attache a new credit card number for given account"),
            @ApiResponse(code = 409, message = "Credit card number already used for other account"),
            @ApiResponse(code = 400, message = "Validation failed for beneficiary data or account id")})
    public AccountDTO addCreditCardToAccount(@PathVariable @ApiParam(value = "Account identifier", required = true) String accountId,
                                             @NotBlank(message = "credit card number is mandatory")
                                             @ApiParam(value = "Card unique number", required = true)
                                             @RequestBody String cardNumber) {
        log.debug("Request to add a credit card number={} to account number {} ", cardNumber, accountId);
        return this.accountService.addCreditCardToAccount(accountId, cardNumber);
    }

    /**
     * {@code DELETE /accounts/:number} : delete the "number" account.
     *
     * @param number the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{accountId}")
    @ApiOperation(value = "Delete account by their identifier")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleting account"),
            @ApiResponse(code = 402, message = "Invalid account id")})
    // @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAccount(@PathVariable(name = "accountId")
                                              @ApiParam(value = "account id ", required = true) String number) {
        log.debug("REST request to delete account: {}", number);
        this.accountService.deleteAccount(number);
        return ResponseEntity.noContent().headers(RestRequestUtils.createAlert(
                this.applicationName, "accountManagement.deleted", number)).build();
    }

    /**
     * Removes the Beneficiary with the given name from the Account with the
     * given id.
     */
    @DeleteMapping(value = "/{accountId}/beneficiaries/{beneficiaryName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation(value = "Delete beneficiary's account by their name identifier")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleting beneficiary for given account"),
            @ApiResponse(code = 402, message = "Invalid account id")})
    public void removeBeneficiary(@ApiParam(value = "account id ", required = true) @PathVariable String accountId
            , @ApiParam(value = "beneficiary registered name", required = true) @PathVariable String beneficiaryName) {
        this.accountService.removeBeneficiary(accountId, beneficiaryName);
    }
}
