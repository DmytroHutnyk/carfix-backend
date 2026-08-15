package com.hutnyk.carfix.booking.repository;

import com.hutnyk.carfix.booking.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
           "LEFT JOIN FETCH b.serviceEntities " +
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
           "LEFT JOIN FETCH b.serviceEntities " +
           "WHERE b.id = :id AND cp.customerEntity.id = :customerId")
    Optional<BookingEntity> findByIdAndCustomerIdWithDetails(@Param("id") UUID id,
                                                             @Param("customerId") UUID customerId);
}
