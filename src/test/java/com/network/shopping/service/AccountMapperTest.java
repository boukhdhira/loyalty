package com.network.shopping.service;

import com.network.shopping.domain.Account;
import com.network.shopping.domain.Beneficiary;
import com.network.shopping.domain.CreditCard;
import com.network.shopping.service.dto.AccountDTO;
import com.network.shopping.service.dto.BeneficiaryDTO;
import com.network.shopping.service.mapper.AccountMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static com.network.shopping.TestUtil.newHashSet;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AccountMapperTest {

    public static final String DEFAULT_ACCOUNT_NAME = "BDRVSPSLMAX";
    public static final String DEFAULT_ACCOUNT_NUMBER = "123456789";
    public static final String DEFAULT_BENEFICIARY_NAME = "PWBRTCSGBGZMPDKG";
    @Autowired
    private AccountMapper mapper;

    private Account account;
    private AccountDTO accountDTO;


    @BeforeEach
    public void init() {
        BeneficiaryDTO beneficiary = new BeneficiaryDTO();
        beneficiary.setName(DEFAULT_BENEFICIARY_NAME);
        beneficiary.setPercentage("100%");

        Beneficiary beneficiary2 = new Beneficiary();
        beneficiary2.setName(DEFAULT_BENEFICIARY_NAME);
        beneficiary2.setAllocationPercentage(BigDecimal.ONE);

        CreditCard creditCard = new CreditCard();
        creditCard.setNumber("15555552224633");

        this.accountDTO = new AccountDTO();
        this.accountDTO.setName(DEFAULT_ACCOUNT_NAME);
        this.accountDTO.setNumber(DEFAULT_ACCOUNT_NUMBER);
        this.accountDTO.setBeneficiaries(newHashSet(beneficiary));
        this.accountDTO.setCreditCards(newHashSet("111111115558222"));

        this.account = new Account();
        this.account.setName(DEFAULT_ACCOUNT_NAME);
        this.account.setNumber(DEFAULT_ACCOUNT_NUMBER);
        this.account.setBeneficiaries(newHashSet(beneficiary2));
        this.account.setCreditCards(newHashSet(creditCard));
    }

    @Test
    public void mapAccountsEntityToAccountDtosAndTakeNullAndEmptyAccounts() throws Exception {
        List<Account> accountsEntities = asList(null, this.account, new Account());
        List<AccountDTO> accounts = this.mapper.toDtos(accountsEntities);

        assertThat(accounts).isNotEmpty();
        assertThat(accounts).size().isEqualTo(3);
    }

}
