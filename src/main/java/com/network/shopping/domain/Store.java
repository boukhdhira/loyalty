package com.network.shopping.domain;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "t_store")
public class Store implements Serializable {
    private static final long serialVersionUID = -1854283599358423202L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @NotNull
    private String merchantNumber;

    @NotNull
    private String name;

    @NotNull
    private BigDecimal benefitsPercentage;

    private String benefitsAvailabilityPolicy;
}
