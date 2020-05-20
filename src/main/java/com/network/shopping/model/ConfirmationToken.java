package com.network.shopping.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.now;
import static javax.persistence.FetchType.EAGER;

/**
 * nullable = false on the User to ensure data integrity and consistency in the
 * unidirectional relationship
 */
@Entity
@Data
@NoArgsConstructor
@Table(name = "t_confirmation_token")
@Accessors(chain = true)
public class ConfirmationToken implements Serializable {
    private static final long serialVersionUID = 227819352036922211L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @OneToOne(targetEntity = User.class, fetch = EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date", nullable = false)
    // @CreatedDate => for audit
    private LocalDateTime createdDate = now();

    //@Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, unique = true, length = 36)
    private String token;
}
