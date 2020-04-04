package com.network.shopping.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * An account for a member of the bonus network. An account has one or more
 * beneficiaries whose allocations must add up to 100%.
 * <p>
 * An account can make contributions to its beneficiaries. Each contribution is
 * distributed among the beneficiaries based on an allocation.
 * <p>
 * An entity. An aggregate.
 */
@Data
@Entity
@Table(name = "t_account", schema = "public")
// use of EqualsAndHashCode and JsonIgnoreProperties to handle circular References/Dependencies
// @EqualsAndHashCode(exclude = {"beneficiaries", "creditCards"})
public class Account implements Serializable {
    private static final long serialVersionUID = -9186125500848619491L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @Column(unique = true, nullable = false)
    private String number;

    @Column(nullable = false)
    private String name;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    //@JsonIgnoreProperties("account")
    private Set<Beneficiary> beneficiaries = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    private Set<CreditCard> creditCards = new HashSet<>();

    @Version
    private int version;

    @Column(name = "user_id", nullable = false)
    private String clientId;
}
