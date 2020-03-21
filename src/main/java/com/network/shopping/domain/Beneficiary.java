package com.network.shopping.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "t_account_beneficiary")
@Data
@JsonIgnoreProperties("beneficiaries")
@EqualsAndHashCode(exclude = "account")
public class Beneficiary implements Serializable {
    private static final long serialVersionUID = 4888973121112637258L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @NotNull
    private String name;

    private BigDecimal allocationPercentage;

    private BigDecimal savings;
}
