package com.hutnyk.carfix.equipment.adapter;

import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentType;
import com.hutnyk.carfix.equipment.entity.EquipmentEntity;
import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import com.hutnyk.carfix.equipment.mapper.EquipmentMapper;
import com.hutnyk.carfix.equipment.repository.EquipmentRepository;
import com.hutnyk.carfix.equipment.repository.EquipmentTypeRepository;
import com.hutnyk.carfix.in.equipment.query.OwnerEquipmentView;
import com.hutnyk.carfix.out.equipment.EquipmentPortOut;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class EquipmentAdapterOut implements EquipmentPortOut {

    private final EquipmentTypeRepository equipmentTypeRepository;
    private final EquipmentRepository equipmentRepository;
    private final EntityManager entityManager;

    @Override
    public EquipmentType insertType(EquipmentType type) {
        BranchEntity branch = type.getBranchId() == null
                ? null
                : entityManager.getReference(BranchEntity.class, type.getBranchId().id());
        return EquipmentMapper.toTypeDomain(
                equipmentTypeRepository.save(EquipmentMapper.toTypeEntity(type, branch)));
    }

    @Override
    public Equipment insert(Equipment equipment) {
        EquipmentTypeEntity type =
                entityManager.getReference(EquipmentTypeEntity.class, equipment.getEquipmentTypeId());
        BranchEntity branch = entityManager.getReference(BranchEntity.class, equipment.getBranchId().id());
        return EquipmentMapper.toDomain(
                equipmentRepository.save(EquipmentMapper.toEntity(equipment, type, branch)));
    }

    @Override
    public Equipment update(Equipment equipment) {
        EquipmentEntity entity = equipmentRepository.findById(equipment.getId())
                .orElseThrow(IllegalStateException::new);
        EquipmentTypeEntity type =
                entityManager.getReference(EquipmentTypeEntity.class, equipment.getEquipmentTypeId());
        EquipmentMapper.updateEntity(entity, equipment, type);
        return EquipmentMapper.toDomain(equipmentRepository.save(entity));
    }

    @Override
    public List<OwnerEquipmentView> findViewsByBranchId(UUID branchId) {
        return equipmentRepository.findAllByBranchEntityId(branchId).stream()
                .map(EquipmentMapper::toOwnerView)
                .toList();
    }

    @Override
    public Optional<Equipment> findByIdAndBranchId(Integer equipmentId, UUID branchId) {
        return equipmentRepository.findByIdAndBranchEntityId(equipmentId, branchId)
                .map(EquipmentMapper::toDomain);
    }

    @Override
    public Optional<EquipmentType> findTypeByNameForBranch(String name, UUID branchId) {
        return equipmentTypeRepository.findByNameForBranch(name, branchId).stream()
                .findFirst()
                .map(EquipmentMapper::toTypeDomain);
    }
}
