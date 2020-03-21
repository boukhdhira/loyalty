package com.network.shopping.domain;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "t_bonus")
@Data
public class Bonus implements Serializable {
    private static final long serialVersionUID = 1951670750403130650L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @CreationTimestamp
    private Date bonusDate;

    private Date shoppingDate;

    @NotNull
    private String confirmationNumber;

    @NotNull
    private double bonusAmount;

    @NotNull
    private String accountNumber;

    @NotNull
    private String productNumber;

    @NotNull
    private double shoppingAmount;
}
