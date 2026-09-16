package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceBayTypeRepository extends JpaRepository<ServiceBayTypeEntity, Integer> {

    @Query("select t from ServiceBayTypeEntity t where t.branchEntity is null or t.branchEntity.id = :branchId")
    List<ServiceBayTypeEntity> findAllForBranch(@Param("branchId") UUID branchId);

    @Query("""
            SELECT t FROM ServiceBayTypeEntity t
            WHERE LOWER(t.name) = LOWER(:name)
              AND (t.branchEntity IS NULL OR t.branchEntity.id = :branchId)
            ORDER BY t.branchEntity.id ASC
            """)
    List<ServiceBayTypeEntity> findByNameForBranch(@Param("name") String name, @Param("branchId") UUID branchId);
}
