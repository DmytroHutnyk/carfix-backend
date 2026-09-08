package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.entity.EmployeeBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeBookingRepository extends JpaRepository<EmployeeBookingEntity, Integer> {

    @Query("SELECT eb FROM EmployeeBookingEntity eb JOIN FETCH eb.employeeEntity e LEFT JOIN FETCH e.roles WHERE eb.bookingEntity.id IN :bookingIds")
    List<EmployeeBookingEntity> findAllWithEmployeeByBookingIds(@Param("bookingIds") Collection<UUID> bookingIds);

    List<EmployeeBookingEntity> findAllByEmployeeEntityIdInAndDate(
            Collection<UUID> employeeIds, LocalDate date);

    List<EmployeeBookingEntity> findAllByEmployeeEntityIdInAndDateBetween(
            Collection<UUID> employeeIds, LocalDate from, LocalDate to);

    List<EmployeeBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
