package com.hutnyk.carfix.repository;

import com.hutnyk.carfix.entity.carProfile.ModelGenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelGenerationRepository extends JpaRepository<ModelGenerationEntity, Integer> {
    List<ModelGenerationEntity> findByCarModelEntityId(Integer modelId);
}
