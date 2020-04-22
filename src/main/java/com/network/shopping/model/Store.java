package com.network.shopping.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Data
@Table(name = "t_store")
public class Store implements Serializable {
    private static final long serialVersionUID = -1854283599358423202L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String merchantNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal benefitsPercentage;

    @Column(nullable = false)
    private String benefitsAvailabilityPolicy;
}
