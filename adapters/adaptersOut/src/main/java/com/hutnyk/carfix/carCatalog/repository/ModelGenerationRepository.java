package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.ModelGenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ModelGenerationRepository extends JpaRepository<ModelGenerationEntity, Integer> {
    List<ModelGenerationEntity> findByCarModelEntityId(Integer modelId);
}
