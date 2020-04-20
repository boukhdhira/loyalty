package com.network.shopping.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "t_user")
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 5158573993899697510L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial", name = "id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(length = 254, unique = true, nullable = false)
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @Column(name = "account_expired")
    private boolean accountExpired = false;

    @Column(name = "account_locked")
    private boolean accountLocked = false;

    @Column(name = "credentials_expired")
    private boolean credentialsExpired = false;

    private boolean enabled = false;

    @Override
    public String toString() {
        return "{" +
                "username='" + this.username + '\'' +
                ", firstName='" + this.firstName + '\'' +
                ", lastName='" + this.lastName + '\'' +
                ", email='" + this.email + '\'' +
                ", roles=" + this.roles +
                ", accountExpired=" + this.accountExpired +
                ", accountLocked=" + this.accountLocked +
                ", credentialsExpired=" + this.credentialsExpired +
                ", enabled=" + this.enabled +
                '}';
    }
}
