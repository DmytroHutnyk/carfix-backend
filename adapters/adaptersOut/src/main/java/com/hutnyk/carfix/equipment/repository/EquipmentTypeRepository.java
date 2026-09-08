package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentTypeEntity, Integer> {

    @Query("""
            SELECT t FROM EquipmentTypeEntity t
            WHERE LOWER(t.name) = LOWER(:name)
              AND (t.branchEntity IS NULL OR t.branchEntity.id = :branchId)
            ORDER BY t.branchEntity.id ASC
            """)
    List<EquipmentTypeEntity> findByNameForBranch(@Param("name") String name, @Param("branchId") UUID branchId);
}
