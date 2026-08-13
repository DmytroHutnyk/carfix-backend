package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    List<EmployeeEntity> findAllByBranchEntityId(UUID branchId);

    @Query("""
            SELECT DISTINCT e FROM EmployeeEntity e
            JOIN FETCH e.roles
            WHERE e.branchEntity.id = :branchId AND e.status = :status
            """)
    List<EmployeeEntity> findAllWithRolesByBranchIdAndStatus(
            @Param("branchId") UUID branchId, @Param("status") EmployeeStatus status);
}
