package com.hutnyk.carfix.openingHours.repository;

import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OpeningHoursExceptionRepository extends JpaRepository<OpeningHoursExceptionEntity, Integer> {

    List<OpeningHoursExceptionEntity> findAllByBranchEntityIdAndDateBetween(UUID branchId, LocalDate from, LocalDate to);
}
