package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModelEntity, Integer> {

    @Query("SELECT cm FROM CarModelEntity cm " +
           "JOIN FETCH cm.carBrandEntity cb " +
           "WHERE cb.id = :brandId")
    List<CarModelEntity> findByCarBrandEntityId(@Param("brandId") Integer brandId);
}
