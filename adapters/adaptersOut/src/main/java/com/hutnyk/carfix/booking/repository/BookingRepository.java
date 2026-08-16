package com.hutnyk.carfix.booking.repository;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingEntity, UUID> {

    @Query("SELECT DISTINCT b FROM BookingEntity b " +
           "JOIN FETCH b.branchEntity br " +
           "JOIN FETCH br.addressEntity a " +
           "JOIN FETCH a.cityEntity " +
           "JOIN FETCH b.carProfileEntity cp " +
           "JOIN FETCH cp.modelVersionEntity mv " +
           "JOIN FETCH mv.carModelEntity cm " +
           "JOIN FETCH cm.carBrandEntity " +
           "LEFT JOIN FETCH b.segments seg " +
           "LEFT JOIN FETCH seg.serviceEntity " +
           "WHERE cp.customerEntity.id = :customerId " +
           "ORDER BY b.date DESC, b.startTime DESC")
    List<BookingEntity> findAllByCustomerIdWithDetails(@Param("customerId") UUID customerId);

    @Query("SELECT b FROM BookingEntity b " +
           "JOIN FETCH b.branchEntity br " +
           "JOIN FETCH br.addressEntity a " +
           "JOIN FETCH a.cityEntity " +
           "JOIN FETCH b.carProfileEntity cp " +
           "JOIN FETCH cp.modelVersionEntity mv " +
           "JOIN FETCH mv.carModelEntity cm " +
           "JOIN FETCH cm.carBrandEntity " +
           "LEFT JOIN FETCH b.segments seg " +
           "LEFT JOIN FETCH seg.serviceEntity " +
           "WHERE b.id = :id AND cp.customerEntity.id = :customerId")
    Optional<BookingEntity> findByIdAndCustomerIdWithDetails(@Param("id") UUID id,
                                                             @Param("customerId") UUID customerId);

    @Query("SELECT COUNT(b) > 0 FROM BookingEntity b " +
           "WHERE b.carProfileEntity.id = :carProfileId AND b.date = :date " +
           "AND b.status IN :statuses AND b.startTime < :end AND b.endTime > :start")
    boolean existsByCarProfileOverlapping(@Param("carProfileId") UUID carProfileId,
                                          @Param("date") LocalDate date,
                                          @Param("start") LocalTime start,
                                          @Param("end") LocalTime end,
                                          @Param("statuses") Collection<BookingStatus> statuses);

    /* Rows: [UUID branchId, LocalDate date, BookingStatus status, Long count]. */
    @Query("""
            SELECT b.branchEntity.id, b.date, b.status, COUNT(b)
            FROM BookingEntity b
            WHERE b.branchEntity.id IN :branchIds AND b.date IN :dates
            GROUP BY b.branchEntity.id, b.date, b.status
            """)
    List<Object[]> countByBranchDateAndStatus(@Param("branchIds") Collection<UUID> branchIds,
                                              @Param("dates") Collection<LocalDate> dates);
}
