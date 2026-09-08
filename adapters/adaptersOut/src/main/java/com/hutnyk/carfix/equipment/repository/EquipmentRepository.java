package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.EquipmentStatus;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<EquipmentEntity, Integer> {

    @EntityGraph(attributePaths = {"equipmentTypeEntity"})
    List<EquipmentEntity> findAllByBranchEntityId(UUID branchId);

    Optional<EquipmentEntity> findByIdAndBranchEntityId(Integer equipmentId, UUID branchId);

    List<EquipmentEntity> findAllByBranchEntityIdAndStatus(UUID branchId, EquipmentStatus status);

    @EntityGraph(attributePaths = {"branchEntity", "equipmentTypeEntity"})
    List<EquipmentEntity> findAllByBranchEntityIdInAndStatus(Collection<UUID> branchIds, EquipmentStatus status);
}
