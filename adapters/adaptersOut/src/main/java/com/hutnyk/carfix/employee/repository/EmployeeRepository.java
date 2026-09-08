package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.entity.EmployeeEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {

    List<EmployeeEntity> findAllByBranchEntityId(UUID branchId);

    Optional<EmployeeEntity> findByIdAndBranchEntityId(UUID id, UUID branchId);

    @Query("""
            SELECT DISTINCT e FROM EmployeeEntity e
            LEFT JOIN FETCH e.roles
            WHERE e.branchEntity.id = :branchId
            """)
    List<EmployeeEntity> findAllWithRolesByBranchId(@Param("branchId") UUID branchId);

    @Query("""
            SELECT DISTINCT e FROM EmployeeEntity e
            JOIN FETCH e.roles
            WHERE e.branchEntity.id = :branchId AND e.status = :status
            """)
    List<EmployeeEntity> findAllWithRolesByBranchIdAndStatus(
            @Param("branchId") UUID branchId, @Param("status") EmployeeStatus status);

    @Query("""
            SELECT DISTINCT e FROM EmployeeEntity e
            JOIN FETCH e.roles
            JOIN FETCH e.branchEntity
            WHERE e.branchEntity.id IN :branchIds AND e.status = :status
            """)
    List<EmployeeEntity> findAllWithRolesByBranchIdInAndStatus(
            @Param("branchIds") Collection<UUID> branchIds, @Param("status") EmployeeStatus status);

    /* Rows: [UUID branchId, Long count]. */
    @Query("""
            SELECT e.branchEntity.id, COUNT(e)
            FROM EmployeeEntity e
            WHERE e.branchEntity.id IN :branchIds AND e.status = :status
            GROUP BY e.branchEntity.id
            """)
    List<Object[]> countByBranchIdsAndStatus(@Param("branchIds") Collection<UUID> branchIds,
                                             @Param("status") EmployeeStatus status);
}
