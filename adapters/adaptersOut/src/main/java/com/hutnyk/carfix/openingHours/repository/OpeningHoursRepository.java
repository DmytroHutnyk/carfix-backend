package com.hutnyk.carfix.openingHours.repository;

import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OpeningHoursRepository extends JpaRepository<OpeningHoursEntity, Integer> {

    List<OpeningHoursEntity> findAllByBranchEntityId(UUID branchId);

    List<OpeningHoursEntity> findAllByBranchEntityIdIn(Collection<UUID> branchIds);

    void deleteAllByBranchEntityId(UUID branchId);
}
