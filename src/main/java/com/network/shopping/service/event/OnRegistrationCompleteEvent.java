package com.network.shopping.service.event;

import com.network.shopping.model.User;
import org.springframework.context.ApplicationEvent;

/**
 * Async event data published after a success sign-up request.
 */
public class OnRegistrationCompleteEvent extends ApplicationEvent {
    private static final long serialVersionUID = 988121766482634390L;
    private final User user;

    public OnRegistrationCompleteEvent(
            final User user) {
        super(user);

        this.user = user;
    }

    /**
     * Getter for property 'user'.
     *
     * @return Value for property 'user'.
     */
    public User getUser() {
        return this.user;
    }
}
