package com.network.shopping.web.rest;

import com.network.shopping.domain.Account;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.dto.AccountDTO;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.network.shopping.TestUtil.asJsonString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link AccountController} REST controller.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {

    public static final String DEFAULT_FIRST_ACCOUNT_NUMBER = "123456789";
    public static final String DEFAULT_FIRST_ACCOUNT_NAME = RandomStringUtils.randomAlphabetic(10);
    @Autowired
    private MockMvc restMockMvc;

    @Autowired
    private AccountRepository repository;

    public static Account createEntity() {
        Account defaultAccount = new Account();
        defaultAccount.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        defaultAccount.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        defaultAccount.setVersion(0);
        return defaultAccount;
    }

    // verify(repository, times(0)).save(any(Account.class));
    //  given(service.getAllEmployees()).willReturn(allEmployees) -> when ... mock service

    @Test
    @Transactional
    public void testSaveValidAccount() throws Exception {
        AccountDTO account = new AccountDTO();
        account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        restMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/accounts")
                .content(asJsonString(account))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        Account createdUser = repository.findOneByNumber(DEFAULT_FIRST_ACCOUNT_NUMBER).orElse(null);
        assertNotNull(createdUser);
        Assertions.assertThat(createdUser.getNumber()).isEqualTo(DEFAULT_FIRST_ACCOUNT_NUMBER);
        Assertions.assertThat(createdUser.getName()).isEqualTo(DEFAULT_FIRST_ACCOUNT_NAME);
        Assertions.assertThat(createdUser.getVersion()).isEqualTo(0);
        Assertions.assertThat(createdUser.getCreditCards()).isEmpty();
        Assertions.assertThat(createdUser.getBeneficiaries()).isEmpty();
    }

    @Test
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGettingAllRegisteredAccounts() throws Exception {
        restMockMvc.perform(get("/api/v1/accounts")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].number", is(DEFAULT_FIRST_ACCOUNT_NUMBER)));
    }

    @Test
    @Transactional
    public void testDeleteAccountByValidId() throws Exception {
        repository.saveAndFlush(createEntity());
        int accountCountBefore = repository.findAll().size();
        restMockMvc.perform(MockMvcRequestBuilders
                .delete("/api/v1/accounts/{accountId}", DEFAULT_FIRST_ACCOUNT_NUMBER)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        List<Account> emptyList = repository.findAll();
        assertThat(accountCountBefore, is(1));
        assertThat(emptyList, empty());
    }

}
