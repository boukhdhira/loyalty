package com.network.shopping.service.mapper;

import com.network.shopping.common.Percentage;
import com.network.shopping.dto.AccountDTO;
import com.network.shopping.dto.BeneficiaryDTO;
import com.network.shopping.model.Account;
import com.network.shopping.model.Beneficiary;
import com.network.shopping.model.CreditCard;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public abstract class AccountMapper {
    //@Mapping(target = "creditCards", ignore = true)
    public abstract Account toEntity(AccountDTO dto);

    public abstract AccountDTO toDto(Account entity);

    public abstract List<AccountDTO> toDtos(List<Account> entity);

    @IterableMapping(qualifiedByName = "toDto")
    protected abstract Set<AccountDTO> entitiesToDTOs(Set<Account> entities);

    protected abstract Set<String> mapCreditCardDataListToCardList(Set<CreditCard> list);

    protected abstract Set<CreditCard> mapCreditCardDtosToCardEntities(Set<String> list);

    @Mapping(target = "percentage", source = "allocationPercentage", qualifiedByName = "toPercentage")
    protected abstract BeneficiaryDTO mapBeneficiaryEntityToBeneficiaryDTO(Beneficiary list);

    @Mapping(target = "allocationPercentage", source = "percentage", qualifiedByName = "toDecimal")
    protected abstract Beneficiary mapBeneficiaryDTOToBeneficiaryEntity(BeneficiaryDTO list);

    protected String mapCreditCardToNumber(final CreditCard card) {
        return card.getNumber();
    }

    protected CreditCard mapCreditCardNumberToCardEntity(final String number) {
        final CreditCard card = new CreditCard();
        card.setNumber(number);
        return card;
    }

    @Named("toPercentage")
    String toPercentage(final BigDecimal decimal) {
        return new Percentage(decimal).toString();
    }

    @Named("toDecimal")
    BigDecimal toDecimal(final String percentage) {
        return Percentage.of(percentage).asBigDecimal();
    }
}
