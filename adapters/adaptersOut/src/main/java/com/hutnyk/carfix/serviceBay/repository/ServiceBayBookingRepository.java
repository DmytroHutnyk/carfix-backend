package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayBookingEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBayBookingRepository extends JpaRepository<ServiceBayBookingEntity, Integer> {

    List<ServiceBayBookingEntity> findAllByServiceBayEntityIdInAndDate(
            Collection<Integer> serviceBayIds, LocalDate date);

    List<ServiceBayBookingEntity> findAllByBookingEntityId(UUID bookingId);

    void deleteAllByBookingEntityId(UUID bookingId);
}
