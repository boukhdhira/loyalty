package com.network.shopping.service;

import com.network.shopping.dto.BonusConfirmationDTO;
import com.network.shopping.dto.ShoppingDTO;

public interface BonusNetwork {
    /**
     * Bonus an account for shopping.
     * <p>
     * For a shopping to be eligible for bonus: - It must have been paid for by a registered credit card of a valid
     * member account in the network. - It must have taken place at a store participating in the network.
     *
     * @param shopping a charge made to a credit card for shopping at a store
     * @return confirmation of the bonus
     */
    public BonusConfirmationDTO bonusAccountFor(ShoppingDTO shopping);
}
