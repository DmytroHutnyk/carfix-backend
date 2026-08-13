package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Integer> {

    List<EquipmentEntity> findAllByBranchEntityId(UUID branchId);

    List<EquipmentEntity> findAllByBranchEntityIdAndStatus(UUID branchId, EquipmentStatus status);
}
