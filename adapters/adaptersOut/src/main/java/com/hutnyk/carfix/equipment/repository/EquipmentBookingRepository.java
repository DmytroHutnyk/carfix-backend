package com.hutnyk.carfix.equipment.repository;

import com.hutnyk.carfix.equipment.entity.EquipmentBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentBookingRepository extends JpaRepository<EquipmentBookingEntity, Integer> {

    @Query("SELECT qb FROM EquipmentBookingEntity qb JOIN FETCH qb.equipmentEntity WHERE qb.bookingEntity.id IN :bookingIds")
    List<EquipmentBookingEntity> findAllWithEquipmentByBookingIds(@Param("bookingIds") Collection<UUID> bookingIds);

    List<EquipmentBookingEntity> findAllByEquipmentEntityIdInAndDate(
            Collection<Integer> equipmentIds, LocalDate date);

    List<EquipmentBookingEntity> findAllByEquipmentEntityIdInAndDateBetween(
            Collection<Integer> equipmentIds, LocalDate from, LocalDate to);

    List<EquipmentBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
