package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayAvailabilityEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBayAvailabilityRepository extends JpaRepository<ServiceBayAvailabilityEntity, Integer> {

    List<ServiceBayAvailabilityEntity> findAllByServiceBayEntityIdInAndDate(
            Collection<Integer> serviceBayIds, LocalDate date);

    List<ServiceBayAvailabilityEntity> findAllByServiceBayEntityIdInAndDateBetween(
            Collection<Integer> serviceBayIds, LocalDate from, LocalDate to);

    List<ServiceBayAvailabilityEntity> findAllBySeriesId(Integer seriesId);
}
