package com.hutnyk.carfix.service.repository;

import com.hutnyk.carfix.service.entity.ServiceEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<ServiceEntity, Integer> {

    List<ServiceEntity> findAllByBranchEntityId(UUID branchId);
}
