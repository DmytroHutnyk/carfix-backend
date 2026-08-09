package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.entity.EquipmentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentTypeRepository extends JpaRepository<EquipmentTypeEntity, Integer> {
}
