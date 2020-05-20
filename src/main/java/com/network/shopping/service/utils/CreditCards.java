package com.network.shopping.service.utils;

import org.springframework.util.CollectionUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.regex.Pattern;

import static com.network.shopping.config.Constants.CREDIT_CARD_NUMBER_REGEX;
import static org.springframework.util.StringUtils.isEmpty;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {SingleCreditCardValidator.class, CreditCardCollectionValidator.class})
public @interface CreditCards {

    String message() default "Invalid credit cards number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

abstract class AbstractCreditCardsValidator<T> implements ConstraintValidator<CreditCards, T> {
    @Override
    public void initialize(final CreditCards creditCards) {
    }

    @Override
    public boolean isValid(final T cards, final ConstraintValidatorContext context) {
        final Pattern pattern = Pattern.compile(CREDIT_CARD_NUMBER_REGEX);
        return this.match(cards, pattern);
    }

    protected abstract boolean match(T cards, Pattern pattern);
}

class SingleCreditCardValidator extends AbstractCreditCardsValidator<String> {

    @Override
    protected boolean match(final String value, final Pattern pattern) {
        return !isEmpty(value) && pattern.matcher(value.replaceAll("\\s+", "")).matches();
    }
}

class CreditCardCollectionValidator extends AbstractCreditCardsValidator<Collection<String>> {

    @Override
    protected boolean match(final Collection<String> values, final Pattern pattern) {
        return CollectionUtils.isEmpty(values) || values.stream().allMatch(nef -> !isEmpty(nef)
                && pattern.matcher(nef.replaceAll("\\s+", "")).matches());
    }

}
