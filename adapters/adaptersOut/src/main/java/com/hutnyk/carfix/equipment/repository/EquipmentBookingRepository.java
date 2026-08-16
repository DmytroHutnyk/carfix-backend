package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.entity.EquipmentBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentBookingRepository extends JpaRepository<EquipmentBookingEntity, Integer> {

    List<EquipmentBookingEntity> findAllByEquipmentEntityIdInAndDate(
            Collection<Integer> equipmentIds, LocalDate date);

    List<EquipmentBookingEntity> findAllByEquipmentEntityIdInAndDateBetween(
            Collection<Integer> equipmentIds, LocalDate from, LocalDate to);

    List<EquipmentBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
