package com.network.shopping.repository;

import com.network.shopping.common.enums.RoleEnum;
import com.network.shopping.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(RoleEnum name);
}
