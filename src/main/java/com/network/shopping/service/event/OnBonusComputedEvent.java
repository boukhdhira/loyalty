package com.network.shopping.service.event;

import com.network.shopping.domain.Bonus;
import org.springframework.context.ApplicationEvent;

public class OnBonusComputedEvent extends ApplicationEvent {
    private static final long serialVersionUID = 7957918878278380772L;
    private final Bonus bonus;

    public OnBonusComputedEvent(final Bonus bonus) {
        super(bonus);
        this.bonus = bonus;
    }

    /**
     * Getter for property 'bonus'.
     *
     * @return Value for property 'bonus'.
     */
    public Bonus getBonus() {
        return this.bonus;
    }
}
