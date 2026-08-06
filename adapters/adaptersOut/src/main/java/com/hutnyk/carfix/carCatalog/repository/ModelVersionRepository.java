package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ModelVersionRepository extends JpaRepository<ModelVersionEntity, Integer> {

    @Query("SELECT mv FROM ModelVersionEntity mv " +
           "JOIN FETCH mv.carModelEntity cm " +
           "WHERE cm.id = :modelId")
    List<ModelVersionEntity> findByCarModelEntityId(@Param("modelId") Integer modelId);
}
