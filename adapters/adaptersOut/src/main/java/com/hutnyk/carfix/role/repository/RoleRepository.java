package com.hutnyk.carfix.role.repository;

import com.hutnyk.carfix.role.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
}
