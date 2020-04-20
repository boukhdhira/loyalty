package com.network.shopping.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "t_account_credit_card")
@Data
@JsonIgnoreProperties("creditCards")
@EqualsAndHashCode(exclude = "account")
public class CreditCard implements Serializable {
    private static final long serialVersionUID = 2879637614226311753L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @NotNull
    @Column(name = "number", unique = true)
    private String number;
}
