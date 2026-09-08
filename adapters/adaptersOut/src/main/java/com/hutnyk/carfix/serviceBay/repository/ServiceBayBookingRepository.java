package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceBayBookingRepository extends JpaRepository<ServiceBayBookingEntity, Integer> {

    @Query("SELECT sb FROM ServiceBayBookingEntity sb JOIN FETCH sb.serviceBayEntity WHERE sb.bookingEntity.id IN :bookingIds")
    List<ServiceBayBookingEntity> findAllWithBayByBookingIds(@Param("bookingIds") Collection<UUID> bookingIds);

    List<ServiceBayBookingEntity> findAllByServiceBayEntityIdInAndDate(
            Collection<Integer> serviceBayIds, LocalDate date);

    List<ServiceBayBookingEntity> findAllByServiceBayEntityIdInAndDateBetween(
            Collection<Integer> serviceBayIds, LocalDate from, LocalDate to);

    List<ServiceBayBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
