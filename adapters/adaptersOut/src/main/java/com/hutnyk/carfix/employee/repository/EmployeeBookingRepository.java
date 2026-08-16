package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.entity.EmployeeBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeBookingRepository extends JpaRepository<EmployeeBookingEntity, Integer> {

    List<EmployeeBookingEntity> findAllByEmployeeEntityIdInAndDate(
            Collection<UUID> employeeIds, LocalDate date);

    List<EmployeeBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
