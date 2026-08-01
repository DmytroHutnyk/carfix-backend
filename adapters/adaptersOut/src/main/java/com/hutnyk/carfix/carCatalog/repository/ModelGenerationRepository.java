package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.ModelGenerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelGenerationRepository extends JpaRepository<ModelGenerationEntity, Integer> {

    @Query("SELECT mg FROM ModelGenerationEntity mg " +
           "JOIN FETCH mg.carModelEntity cm " +
           "WHERE cm.id = :modelId")
    List<ModelGenerationEntity> findByCarModelEntityId(@Param("modelId") Integer modelId);
}
