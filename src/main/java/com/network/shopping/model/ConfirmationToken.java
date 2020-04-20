package com.network.shopping.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * nullable = false on the User to ensure data integrity and consistency in the
 * unidirectional relationship
 */
@Entity
@Data
@NoArgsConstructor
public class ConfirmationToken implements Serializable {
    private static final long serialVersionUID = 227819352036922211L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Long id;
    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @Temporal(TemporalType.TIMESTAMP)
    // @CreatedDate => for audit
    private Date createdDate = new Date();
    @Column(nullable = false, unique = true)
    private String token;

    public ConfirmationToken(final String token, final User user) {
        this.token = token;
        this.user = user;
    }
}
