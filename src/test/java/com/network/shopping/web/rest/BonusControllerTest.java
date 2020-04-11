package com.network.shopping.web.rest;

import com.network.shopping.domain.*;
import com.network.shopping.repository.AccountRepository;
import com.network.shopping.repository.BonusRepository;
import com.network.shopping.repository.StoreRepository;
import com.network.shopping.repository.UserRepository;
import com.network.shopping.service.dto.ShoppingDTO;
import com.network.shopping.service.event.OnBonusComputedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static com.network.shopping.TestUtil.asJsonString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class BonusControllerTest {

    public static final String DEFAULT_FIRST_ACCOUNT_NUMBER = "123456789";
    public static final String DEFAULT_CLIENT_ID = "user";
    public static final String DEFAULT_FIRST_ACCOUNT_NAME = randomAlphabetic(10);
    public static final String DEFAULT_CREDIT_CARD = "2222222222222";
    public static final String DEFAULT_MERCHANT_NUMBER = "1022000001";
    public static final String PURCHASE_CREDIT_CARD_NUMBER = "4111 1111 1111 1111";
    public static final String PURCHASE_DATE = "2008-10-03";
    public static final BigDecimal PURCHASE_AMOUNT = BigDecimal.valueOf(100);
    public static final BigDecimal BENEFITS_PERCENTAGE = BigDecimal.valueOf(0.02);
    // Some fixed date to make your tests
    private final static LocalDate LOCAL_DATE = LocalDate.of(2008, 11, 13);
    ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

    @MockBean
    private StoreRepository storeRepository;
    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AccountRepository accountRepository;
    @MockBean
    private Clock clock;
    @Autowired
    private WebApplicationContext context;
    @Autowired
    private BonusRepository bonusRepository;

    private MockMvc restMockMvc;

    private Account account;
    private Store store;

    @BeforeEach
    public void setup() {
        this.restMockMvc = MockMvcBuilders
                .webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();

        final Clock fixedClock = Clock.fixed(LOCAL_DATE.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        when(this.clock.instant()).thenReturn(fixedClock.instant());
        when(this.clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @BeforeEach
    public void setUp() {
        final Beneficiary beneficiary1 = new Beneficiary();
        beneficiary1.setSavings(BigDecimal.ZERO);
        beneficiary1.setAllocationPercentage(BigDecimal.valueOf(0.4));

        final Beneficiary beneficiary2 = new Beneficiary();
        beneficiary2.setSavings(BigDecimal.ZERO);
        beneficiary2.setAllocationPercentage(BigDecimal.valueOf(0.6));

        final CreditCard card = new CreditCard();
        card.setNumber(DEFAULT_CREDIT_CARD);

        this.account = new Account();
        this.account.setName(DEFAULT_FIRST_ACCOUNT_NAME);
        this.account.setNumber(DEFAULT_FIRST_ACCOUNT_NUMBER);
        this.account.setClientId(DEFAULT_CLIENT_ID);
        this.account.setVersion(0);
        this.account.setBeneficiaries(new HashSet<>(Arrays.asList(beneficiary1, beneficiary2)));
        this.account.setCreditCards(new HashSet<>(Collections.singletonList(card)));


        this.store = new Store();
        this.store.setMerchantNumber(DEFAULT_MERCHANT_NUMBER);
        this.store.setBenefitsPercentage(BENEFITS_PERCENTAGE);
        when(this.storeRepository.findByMerchantNumber(anyString())).thenReturn(Optional.of(this.store));
    }

    @Test
    @Transactional
    public void shouldSaveBonusOperationAndComputeContritionToEachBenedictoryAccordingToTheirPercentage() throws Exception {
        //when
        final ShoppingDTO shopping = new ShoppingDTO();
        shopping.setMerchantNumber(DEFAULT_MERCHANT_NUMBER);
        shopping.setDate(PURCHASE_DATE);
        shopping.setAmount(PURCHASE_AMOUNT);
        shopping.setCreditCardNumber(PURCHASE_CREDIT_CARD_NUMBER);

        final User user = new User();
        user.setEmail("IDIDIIDDI@DKDKDKKD.KDKDK");

        when(this.accountRepository.findByCreditCardsNumber(anyString())).thenReturn(Optional.of(this.account));
        when(this.accountRepository.findOneByNumber(anyString())).thenReturn(Optional.of(this.account));
        when(this.userRepository.findOneByUsername(anyString())).thenReturn(Optional.of(user));

        final ApplicationEventPublisher eventPublisher = spy(ApplicationEventPublisher.class);
        doNothing().when(eventPublisher).publishEvent(any(OnBonusComputedEvent.class));

        final List<Bonus> initialBonusList = this.bonusRepository.findAll();
        assertTrue(initialBonusList.isEmpty());

        this.restMockMvc.perform(MockMvcRequestBuilders.post("/api/v1/bonus")
                .content(asJsonString(shopping))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmationNumber", is(notNullValue())));

        //verify(this.eventPublisher).publishEvent(any(Bonus.class));

        // verify saving operation
        final List<Bonus> registeredBonus = this.bonusRepository.findAll();
        assertAll("bonus",
                () -> assertFalse(registeredBonus.isEmpty()),
                () -> assertEquals(1, registeredBonus.size()),
                () -> assertEquals(PURCHASE_AMOUNT, registeredBonus.get(0).getShoppingAmount()),
                () -> assertEquals(PURCHASE_DATE, registeredBonus.get(0).getShoppingDate().toString()),
                () -> assertEquals(this.store.getMerchantNumber(), registeredBonus.get(0).getProductNumber()),
                () -> assertEquals(2, registeredBonus.get(0).getBonusAmount().doubleValue()),
                () -> assertEquals(LocalDate.now(this.clock), registeredBonus.get(0).getBonusDate())
        );

        // verify saving beneficiary contribution transaction
        verify(this.accountRepository, times(1)).save(this.captor.capture());
        final Account savedAccount = this.captor.getValue();
        final Beneficiary firstBeneficiary = new ArrayList<>(savedAccount.getBeneficiaries()).get(0);
        final Beneficiary secondBeneficiary = new ArrayList<>(savedAccount.getBeneficiaries()).get(1);
        assertEquals(0.8, firstBeneficiary.getSavings().doubleValue());
        assertEquals(1.2, secondBeneficiary.getSavings().doubleValue());
    }
}
