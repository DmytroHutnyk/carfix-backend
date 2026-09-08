package com.hutnyk.carfix.role.repository;

import com.hutnyk.carfix.role.entity.RoleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {

    @Query("SELECT r FROM RoleEntity r WHERE r.branchEntity IS NULL OR r.branchEntity.id = :branchId")
    List<RoleEntity> findAllForBranch(@Param("branchId") UUID branchId);
}
