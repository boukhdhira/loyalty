package com.network.shopping.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "t_bonus")
@Data
public class Bonus implements Serializable {
    private static final long serialVersionUID = 1951670750403130650L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    //@CreationTimestamp -> compliqué pour les tests unitaire de plus elle est utilisé pour l'audit
    @Column(name = "bonus_date")
    private LocalDate bonusDate;

    @Column(nullable = false, name = "shopping_date")
    private LocalDate shoppingDate;

    @Column(nullable = false, unique = true, name = "confirmation_number")
    private String confirmationNumber;

    @Column(nullable = false, name = "bonus_amount")
    private BigDecimal bonusAmount;

    @Column(nullable = false, name = "account_number")
    private String accountNumber;

    @Column(nullable = false, name = "product_number")
    private String productNumber;

    @Column(nullable = false, name = "shopping_amount")
    private BigDecimal shoppingAmount;
}
