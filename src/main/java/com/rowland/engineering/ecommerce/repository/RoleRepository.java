package com.rowland.engineering.ecommerce.repository;

import com.rowland.engineering.ecommerce.model.Role;
import com.rowland.engineering.ecommerce.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName roleName);
}
