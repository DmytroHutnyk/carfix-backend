package com.hutnyk.carfix.employee.repository;

import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.entity.EmployeeAvailabilityEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeAvailabilityRepository extends JpaRepository<EmployeeAvailabilityEntity, Integer> {

    List<EmployeeAvailabilityEntity> findAllByEmployeeEntityIdInAndDate(
            Collection<UUID> employeeIds, LocalDate date);

    List<EmployeeAvailabilityEntity> findAllByEmployeeEntityIdInAndDateBetween(
            Collection<UUID> employeeIds, LocalDate from, LocalDate to);

    List<EmployeeAvailabilityEntity> findAllBySeriesId(Integer seriesId);

    /* Rows: [UUID branchId, LocalDate date, Long distinctEmployees]. */
    @Query("""
            SELECT e.branchEntity.id, a.date, COUNT(DISTINCT e.id)
            FROM EmployeeAvailabilityEntity a
            JOIN a.employeeEntity e
            WHERE e.branchEntity.id IN :branchIds AND e.status = :status AND a.date IN :dates
            GROUP BY e.branchEntity.id, a.date
            """)
    List<Object[]> countDistinctEmployeesByBranchAndDate(@Param("branchIds") Collection<UUID> branchIds,
                                                        @Param("status") EmployeeStatus status,
                                                        @Param("dates") Collection<LocalDate> dates);
}
