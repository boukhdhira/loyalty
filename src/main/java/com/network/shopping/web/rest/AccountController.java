package com.network.shopping.web.rest;

import com.network.shopping.dto.AccountDTO;
import com.network.shopping.dto.BeneficiaryDTO;
import com.network.shopping.service.AccountService;
import com.network.shopping.service.utils.RestRequestUtils;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

/**
 * A controller handling requests for CRUD operations on Accounts and their
 * Beneficiaries.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@Validated
@Slf4j
@Api(value = "Account management endpoint")
public class AccountController {

    private final AccountService accountService;

    @Value("${application.name}")
    private String applicationName;

    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @ApiOperation(value = "retrieve list of all registered accounts", response = Page.class
            , authorizations = {@Authorization(value = "apiKey")})
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody
    Page<AccountDTO> accountSummary(final Pageable pageable) {
        return this.accountService.getAllAccounts(pageable);
    }

    @GetMapping(value = "/{accountId}")
    @ApiOperation(value = "Getting account details by id", authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved account"),
            @ApiResponse(code = 404, message = "The account you were trying to reach is not found or id invalid")
    })
    public ResponseEntity retrieveAccountDetails(@PathVariable(name = "accountId") final String number, final Principal principal) {
        log.debug("Request to retrieve account by number= {}", number);
        return ResponseEntity.ok(this.accountService.getUserAccountByNumber(number, principal.getName()));
    }

    @ApiOperation(value = "update account information", authorizations = {@Authorization(value = "apiKey")})
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated account"),
            @ApiResponse(code = 400, message = "Validation failed for account data")})
    public void updateAccount(@RequestBody @NotNull @Valid final AccountDTO account, final Principal principal) {
        log.debug("Request to update account {} ", account);
        this.accountService.updateUserAccount(account, principal.getName());
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

    @ApiOperation(value = "Attache a new beneficiary to an existing account", authorizations = {@Authorization(value = "apiKey")})
    @PostMapping("/{accountId}/beneficiary")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully save a new beneficiary for given account"),
            @ApiResponse(code = 400, message = "Validation failed for beneficiary data or account id")})
    public AccountDTO addBeneficiaryToAccount(@PathVariable @ApiParam(value = "account identifier", required = true) final
                                              String accountId,
                                              @Valid @ApiParam(value = "list of account beneficiaries with there " +
                                                      "percentage", required = true) @NotEmpty(message = "Input beneficiaries list cannot be empty.")
                                              @RequestBody final List<@Valid BeneficiaryDTO> beneficiaryDTOS, final Principal principal) {
        log.debug("Request to add {} beneficiaries to account number {} ", beneficiaryDTOS.size(), accountId);
        final String authenticatedUserId = principal.getName();
        return this.accountService.addBeneficiariesToAccount(accountId, beneficiaryDTOS, authenticatedUserId);
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
    @ApiOperation(value = "Add credit card number to existing account", authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully attache a new credit card number for given account"),
            @ApiResponse(code = 409, message = "Credit card number already used for other account"),
            @ApiResponse(code = 400, message = "Validation failed for beneficiary data or account id")})
    public AccountDTO addCreditCardToAccount(@PathVariable @ApiParam(value = "Account identifier", required = true) final String accountId,
                                             @NotBlank(message = "credit card number is mandatory")
                                             @ApiParam(value = "Card unique number", required = true)
                                             @RequestBody final String cardNumber,
                                             final Principal principal) {
        log.debug("Request to add a credit card number={} to account number {} ", cardNumber, accountId);
        return this.accountService.addCreditCardToAccount(accountId, cardNumber, principal.getName());
    }

    /**
     * {@code DELETE /accounts/:number} : delete the "number" account.
     *
     * @param number the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{accountId}")
    @ApiOperation(value = "Delete account by their identifier", authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleting account"),
            @ApiResponse(code = 402, message = "Invalid account id")})
    // @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAccount(@PathVariable(name = "accountId")
                                              @ApiParam(value = "account id ", required = true) final String number) {
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
    @ApiOperation(value = "Delete beneficiary's account by their name identifier"
            , authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleting beneficiary for given account"),
            @ApiResponse(code = 402, message = "Invalid account id")})
    public void removeBeneficiary(@ApiParam(value = "account id ", required = true) @PathVariable final String accountId
            , @ApiParam(value = "beneficiary registered name", required = true) @PathVariable final String beneficiaryName,
                                  final Principal principal) {
        this.accountService.removeBeneficiary(accountId, beneficiaryName, principal.getName());
    }

    @PatchMapping(value = "/{accountId}/beneficiaries/{beneficiaryName}")
    @ApiOperation(value = "modified beneficiary percentage", authorizations = {@Authorization(value = "apiKey")})
    public ResponseEntity modifyBeneficiaryPercentage(@ApiParam(value = "account id ", required = true) @PathVariable final String accountId
            , @ApiParam(value = "beneficiary registered name", required = true) @PathVariable final String beneficiaryName,
                                                      @RequestBody @NotNull @Valid final BeneficiaryDTO beneficiary,
                                                      final Principal principal) {
        this.accountService.updateBeneficiaryPercentage(accountId, beneficiaryName, beneficiary, principal.getName());
        return ResponseEntity.ok("beneficiary percentage updated");
    }
}
