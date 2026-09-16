package com.hutnyk.carfix.service.repository;

import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {

    List<ServiceEntity> findAllByBranchEntityId(UUID branchId);

    Optional<ServiceEntity> findByIdAndBranchEntityId(Integer serviceId, UUID branchId);

    @Query("""
            SELECT DISTINCT s FROM ServiceEntity s
            JOIN FETCH s.branchEntity
            JOIN FETCH s.serviceCategoryEntity c
            LEFT JOIN FETCH s.serviceBayTypes
            LEFT JOIN FETCH s.employeeRequirements er
            LEFT JOIN FETCH er.roles
            LEFT JOIN FETCH s.equipmentRequirements qr
            LEFT JOIN FETCH qr.equipmentTypes
            WHERE s.branchEntity.id = :branchId
            ORDER BY c.name, s.name
            """)
    List<ServiceEntity> findAllWithRequirementsByBranchId(@Param("branchId") UUID branchId);

    @Query("""
            SELECT DISTINCT s FROM ServiceEntity s
            JOIN FETCH s.branchEntity
            JOIN FETCH s.serviceCategoryEntity
            LEFT JOIN FETCH s.serviceBayTypes
            LEFT JOIN FETCH s.employeeRequirements er
            LEFT JOIN FETCH er.roles
            LEFT JOIN FETCH s.equipmentRequirements qr
            LEFT JOIN FETCH qr.equipmentTypes
            WHERE s.id = :serviceId AND s.branchEntity.id = :branchId
            """)
    Optional<ServiceEntity> findWithRequirementsByIdAndBranchId(
            @Param("serviceId") Integer serviceId, @Param("branchId") UUID branchId);

    @Query("SELECT COUNT(bs) > 0 FROM BookingSegmentEntity bs WHERE bs.serviceEntity.id = :serviceId")
    boolean existsBookingReference(@Param("serviceId") Integer serviceId);

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
            JOIN FETCH s.branchEntity
            JOIN FETCH s.serviceCategoryEntity
            LEFT JOIN FETCH s.serviceBayTypes
            LEFT JOIN FETCH s.employeeRequirements er
            LEFT JOIN FETCH er.roles
            LEFT JOIN FETCH s.equipmentRequirements qr
            LEFT JOIN FETCH qr.equipmentTypes
            WHERE s.id IN :serviceIds
            """)
    List<ServiceEntity> findAllWithRequirementsByIdIn(@Param("serviceIds") Collection<Integer> serviceIds);
}
