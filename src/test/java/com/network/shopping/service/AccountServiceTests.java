package com.network.shopping.service;

import com.network.shopping.common.Percentage;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static com.network.shopping.common.Percentage.zero;
import static com.network.shopping.web.rest.AccountControllerTest.createEntity;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 * Account service integration tests
 */
@SpringBootTest
public class AccountServiceTests {
    public static final String DEFAULT_FIRST_ACCOUNT_NUMBER = "123456789";
    public static final String TWENTY_PERCENTAGE = "20%";
    public static final String DEFAULT_CLIENT_ID = "user";
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Test
    @Sql("/static/account-data.sql")
    @Sql(value = "/static/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void shouldAddNewBeneficiariesListAndComputePercentageForPareRegisteredBeneficiaries() throws Exception {
        // beneficiary1
        BeneficiaryDTO beneficiary1 = new BeneficiaryDTO();
        beneficiary1.setName(randomAlphabetic(10));
        beneficiary1.setPercentage(TWENTY_PERCENTAGE);

        //beneficiary2
        BeneficiaryDTO beneficiary2 = new BeneficiaryDTO();
        beneficiary2.setName(randomAlphabetic(20));
        beneficiary2.setPercentage(TWENTY_PERCENTAGE);
        AccountDTO newAccountData = this.accountService.addBeneficiariesToAccount(DEFAULT_FIRST_ACCOUNT_NUMBER, asList(beneficiary1, beneficiary2), DEFAULT_CLIENT_ID);

        assertAll(
                () -> assertNotNull(newAccountData),
                () -> assertFalse(newAccountData.getBeneficiaries().isEmpty()),
                () -> assertEquals(4, newAccountData.getBeneficiaries().size()),
                () -> assertEquals(Percentage.oneHundred(), newAccountData.getBeneficiaries().stream().map(
                        b -> Percentage.of(b.getPercentage())).reduce(zero(), Percentage::add))
        );
    }

    @Test
    @Transactional
    public void shouldAddNewBeneficiariesListWithPareRegisteredBeneficiaries() throws Exception {
        this.accountRepository.saveAndFlush(createEntity());

        BeneficiaryDTO beneficiary = new BeneficiaryDTO();
        beneficiary.setName(randomAlphabetic(20));
        beneficiary.setPercentage(TWENTY_PERCENTAGE);
        AccountDTO newAccountData = this.accountService.addBeneficiariesToAccount(DEFAULT_FIRST_ACCOUNT_NUMBER
                , singletonList(beneficiary), DEFAULT_CLIENT_ID);

        assertAll(
                () -> assertNotNull(newAccountData),
                () -> assertFalse(newAccountData.getBeneficiaries().isEmpty()),
                () -> assertEquals(1, newAccountData.getBeneficiaries().size()),
                () -> assertEquals(Percentage.oneHundred(), Percentage.of(newAccountData.getBeneficiaries().stream().findFirst().get().getPercentage())
                )
        );
    }
}
