package com.hutnyk.carfix.service.repository;

import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {

    List<ServiceEntity> findAllByBranchEntityId(UUID branchId);

    @Query("""
            SELECT s FROM ServiceEntity s
            JOIN FETCH s.serviceCategoryEntity c
            WHERE s.branchEntity.id = :branchId AND s.status = :status
            ORDER BY c.name, s.name
            """)
    List<ServiceEntity> findAllWithCategoryByBranchIdAndStatus(
            @Param("branchId") UUID branchId, @Param("status") ServiceStatus status);

    @Query("""
            SELECT DISTINCT s FROM ServiceEntity s
            LEFT JOIN FETCH s.serviceBayTypes
            LEFT JOIN FETCH s.employeeRequirements er
            LEFT JOIN FETCH er.roles
            LEFT JOIN FETCH s.equipmentRequirements qr
            LEFT JOIN FETCH qr.equipmentTypes
            WHERE s.id IN :serviceIds
            """)
    List<ServiceEntity> findAllWithRequirementsByIdIn(@Param("serviceIds") Collection<Integer> serviceIds);
}
