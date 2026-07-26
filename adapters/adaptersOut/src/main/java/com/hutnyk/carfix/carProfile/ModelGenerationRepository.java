package com.hutnyk.carfix.carProfile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelGenerationRepository extends JpaRepository<ModelGenerationEntity, Integer> {
    List<ModelGenerationEntity> findByCarModelEntityId(Integer modelId);
}
