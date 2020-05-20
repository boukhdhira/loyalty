package com.network.shopping.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "t_account_credit_card")
@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude = "account")
public class CreditCard implements Serializable {
    private static final long serialVersionUID = 2879637614226311753L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    private Account account;

    @Column(unique = true, nullable = false)
    private String number;
}
