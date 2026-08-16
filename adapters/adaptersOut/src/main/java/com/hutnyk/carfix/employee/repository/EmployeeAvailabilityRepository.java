package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.entity.EmployeeAvailabilityEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailabilityEntity, Integer> {

    List<EmployeeAvailabilityEntity> findAllByEmployeeEntityIdInAndDate(
            Collection<UUID> employeeIds, LocalDate date);

    List<EmployeeAvailabilityEntity> findAllByEmployeeEntityIdInAndDateBetween(
            Collection<UUID> employeeIds, LocalDate from, LocalDate to);

    List<EmployeeAvailabilityEntity> findAllBySeriesId(Integer seriesId);
}
