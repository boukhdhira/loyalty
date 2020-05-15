package com.network.shopping.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "t_account_beneficiary")
@Data
@EqualsAndHashCode(exclude = "account")
public class Beneficiary implements Serializable {
    private static final long serialVersionUID = 4888973121112637258L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @NotNull
    private String name;

    @Column(precision = 3, scale = 2)
    private BigDecimal allocationPercentage;

    private BigDecimal savings = BigDecimal.ZERO;
}
