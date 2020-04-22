package com.network.shopping.service.mapper;

import com.google.common.collect.ImmutableSet;
import com.network.shopping.common.enums.RoleEnum;
import com.network.shopping.model.Role;
import com.network.shopping.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AuthorityMapper {

    private final RoleRepository roleRepository;

    @Autowired
    public AuthorityMapper(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }


    @AuthorityMapping
    public Set<Role> getAppropriateUserRole(final boolean isAdmin) {
        final Set<Role> authorities = new HashSet<>(ImmutableSet.of(this.roleRepository.findByName(RoleEnum.USER)));
        if (isAdmin) {
            authorities.add(this.roleRepository.findByName(RoleEnum.ADMIN));
        }
        return authorities;
    }
}
