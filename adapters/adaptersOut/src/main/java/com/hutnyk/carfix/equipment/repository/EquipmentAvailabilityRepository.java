package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.entity.EquipmentAvailabilityEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentAvailabilityRepository extends JpaRepository<EquipmentAvailabilityEntity, Integer> {

    List<EquipmentAvailabilityEntity> findAllByEquipmentEntityIdInAndDate(
            Collection<Integer> equipmentIds, LocalDate date);

    List<EquipmentAvailabilityEntity> findAllByEquipmentEntityIdInAndDateBetween(
            Collection<Integer> equipmentIds, LocalDate from, LocalDate to);

    List<EquipmentAvailabilityEntity> findAllBySeriesId(Integer seriesId);
}
