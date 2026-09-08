package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.ServiceBayStatus;
import com.hutnyk.carfix.serviceBay.entity.ServiceBayEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBayRepository extends JpaRepository<ServiceBayEntity, Integer> {

    @EntityGraph(attributePaths = {"serviceBayTypeEntity"})
    List<ServiceBayEntity> findAllByBranchEntityId(UUID branchId);

    @EntityGraph(attributePaths = {"serviceBayTypeEntity"})
    Optional<ServiceBayEntity> findByIdAndBranchEntityId(Integer id, UUID branchId);

    List<ServiceBayEntity> findAllByBranchEntityIdAndStatus(UUID branchId, ServiceBayStatus status);

    @EntityGraph(attributePaths = {"branchEntity", "serviceBayTypeEntity"})
    List<ServiceBayEntity> findAllByBranchEntityIdInAndStatus(Collection<UUID> branchIds, ServiceBayStatus status);
}
