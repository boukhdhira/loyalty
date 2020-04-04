package com.network.shopping.web.rest;

import com.network.shopping.domain.Account;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Optional;

import static com.network.shopping.TestUtil.asJsonString;
import static com.network.shopping.TestUtil.newHashSet;
import static java.util.Collections.singleton;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link AccountController} REST controller.
 */
@SpringBootTest
public class AccountControllerTest {

    public static final String DEFAULT_FIRST_ACCOUNT_NUMBER = "123456789";
    public static final String DEFAULT_CLIENT_ID = "user";
    public static final String OTHER_FIRST_ACCOUNT_NUMBER = "123456000";
    public static final String DEFAULT_FIRST_ACCOUNT_NAME = randomAlphabetic(10);
    public static final String DEFAULT_CREDIT_CARD_NUMBER = "1234123412340001";
    public static final String DEFAULT_BENEFICIARY_NAME = "Dana";
    public static final String DEFAULT_USER = "user";
    public static final String DEFAULT_ADMINISTRATOR = "admin";
    private MockMvc restMockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private AccountRepository repository;

    public static Account createEntity() {
        Account defaultAccount = new Account();
        defaultAccount.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        defaultAccount.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        defaultAccount.setClientId(DEFAULT_CLIENT_ID);
        defaultAccount.setVersion(0);
        return defaultAccount;
    }

    @BeforeEach
    public void setup() {
        this.restMockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    // verify(repository, times(0)).save(any(Account.class)); --> Validation errors
    //  given(service.getAllEmployees()).willReturn(allEmployees) -> when ... mock service

    @WithMockUser(value = DEFAULT_USER)
    @Test
    @Transactional
    public void shouldUpdateExistingAccount() throws Exception {
        this.repository.save(createEntity());
        AccountDTO account = new AccountDTO();
        account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        BeneficiaryDTO beneficiary = new BeneficiaryDTO();
        beneficiary.setPercentage("80%");
        beneficiary.setName("Alia");
        account.setBeneficiaries(singleton(beneficiary));
        account.setCreditCards(singleton("0522220"));

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Account createdUser = this.repository.findOneByNumber(DEFAULT_FIRST_ACCOUNT_NUMBER).orElse(null);
        assertNotNull(createdUser);
        Assertions.assertThat(createdUser.getNumber()).isEqualTo(DEFAULT_FIRST_ACCOUNT_NUMBER);
        Assertions.assertThat(createdUser.getName()).isEqualTo(DEFAULT_FIRST_ACCOUNT_NAME);
        Assertions.assertThat(createdUser.getVersion()).isEqualTo(1);
        Assertions.assertThat(createdUser.getCreditCards()).isNotEmpty();
        Assertions.assertThat(createdUser.getCreditCards().size()).isEqualTo(1);
        Assertions.assertThat(createdUser.getCreditCards().stream().findFirst().get().getNumber()).isEqualTo("0522220");
        Assertions.assertThat(createdUser.getBeneficiaries()).isNotEmpty();
        Assertions.assertThat(createdUser.getBeneficiaries().size()).isEqualTo(1);
        //Assertions.assertThat(createdUser.getBeneficiaries().stream().findFirst().get().getAllocationPercentage()).isEqualTo(new BigDecimal(100));
        Assertions.assertThat(createdUser.getBeneficiaries().stream().findFirst().get().getName()).isEqualTo("Alia");
    }

    @WithMockUser(value = DEFAULT_ADMINISTRATOR, roles = {"ADMIN"})
    @Test
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGettingAllRegisteredAccounts() throws Exception {
        this.restMockMvc.perform(get("/api/v1/accounts")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].number", is(DEFAULT_FIRST_ACCOUNT_NUMBER)));
    }

    @WithMockUser(value = DEFAULT_ADMINISTRATOR)
    @Test
    @Transactional
    public void testDeleteAccountByValidId() throws Exception {
        this.repository.saveAndFlush(createEntity());
        int accountCountBefore = this.repository.findAll().size();
        this.restMockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/accounts/{accountId}", DEFAULT_FIRST_ACCOUNT_NUMBER)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        List<Account> emptyList = this.repository.findAll();
        assertThat(accountCountBefore, is(1));
        assertThat(emptyList, empty());
    }


    @Test
    @Disabled
    @Deprecated
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(value = DEFAULT_USER)
    void shouldReturnConflictRequestWhenAccountNumberAlreadyUsed() throws Exception {
        AccountDTO account = new AccountDTO();
        account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);

        this.restMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        //  verify(service, times(0)).addAccount(any(AccountDTO.class));
    }

    @Test
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @WithMockUser(value = DEFAULT_USER)
    void shouldReturnConflictRequestWhenCreditCardNumberIsAlreadyUsedForOtherAccount() throws Exception {
        AccountDTO account = new AccountDTO();
        account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        account.setCreditCards(singleton(DEFAULT_CREDIT_CARD_NUMBER));

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @WithMockUser(value = DEFAULT_USER)
    @Test
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldRemoveAccountBeneficiaryByValidAccountIdAndBeneficiaryName() throws Exception {

        this.restMockMvc.perform(delete("/api/v1/accounts/{accountId}/beneficiaries/{beneficiaryName}",
                DEFAULT_FIRST_ACCOUNT_NUMBER, DEFAULT_BENEFICIARY_NAME)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        Optional<Account> account = this.repository.findOneByNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        assertTrue(account.isPresent());
        //assertThat(account.map(b -> b.getBeneficiaries().size()).orElse(0), equalTo(1));
    }

    @WithMockUser(value = DEFAULT_USER)
    @Test
    void shouldReturnBadRequestWhenInputDataHasInvalidFormat() throws Exception {
        AccountDTO account = new AccountDTO();
        account.setName("");
        account.setNumber(OTHER_FIRST_ACCOUNT_NUMBER);

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        AccountDTO secondAccount = new AccountDTO();
        secondAccount.setName(DEFAULT_FIRST_ACCOUNT_NAME);

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        AccountDTO thirdAccount = new AccountDTO();
        thirdAccount.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        thirdAccount.setNumber(RandomStringUtils.random(5));

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(value = DEFAULT_USER)
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldReturnBadRequestWhenBeneficiariesHasASumOfAllocationPercentageGreaterThen100() throws Exception {
        BeneficiaryDTO beneficiary1 = new BeneficiaryDTO();
        beneficiary1.setPercentage("80%");
        beneficiary1.setName(randomAlphabetic(20));

        BeneficiaryDTO beneficiary2 = new BeneficiaryDTO();
        beneficiary2.setPercentage("40%");
        beneficiary2.setName(randomAlphabetic(20));

        AccountDTO account = new AccountDTO();
        account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        account.setBeneficiaries(newHashSet(beneficiary1, beneficiary2));

        this.restMockMvc.perform(MockMvcRequestBuilders.put("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(value = DEFAULT_USER)
    void shouldReturn404NotFoundWhenAccountIdIsNotReattachedToAnyAccount() throws Exception {
        this.restMockMvc.perform(get("/api/v1/accounts/{accountId}", RandomStringUtils.random(9))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @WithMockUser(value = DEFAULT_USER)
    void shouldReturnAccountDetailsWhenAccountIdExist() throws Exception {
        this.repository.save(createEntity());

        this.restMockMvc.perform(get("/api/v1/accounts/{accountId}", DEFAULT_FIRST_ACCOUNT_NUMBER)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.number").value(DEFAULT_FIRST_ACCOUNT_NUMBER))
                .andExpect(jsonPath("$.name").value(DEFAULT_FIRST_ACCOUNT_NAME))
                .andExpect(jsonPath("$.beneficiaries").isEmpty())
                .andExpect(jsonPath("$.creditCards").isEmpty());
    }
}
