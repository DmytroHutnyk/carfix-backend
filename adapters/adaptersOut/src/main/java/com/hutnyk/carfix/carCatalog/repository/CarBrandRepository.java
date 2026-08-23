package com.hutnyk.carfix.carCatalog.repository;

import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CarBrandRepository extends JpaRepository<CarBrandEntity, Integer> {

    /* Native: car_brands_branches is a bare join table with no entity/association mapped over it. */
    @Query(nativeQuery = true, value = """
            SELECT cb.* FROM car_brands cb
            JOIN car_brands_branches cbb ON cbb.car_brand_id = cb.car_brand_id
            WHERE cbb.branch_id = :branchId
            ORDER BY cb.name
            """)
    List<CarBrandEntity> findAllByBranchId(@Param("branchId") UUID branchId);

    @Modifying(flushAutomatically = true)
    @Query(nativeQuery = true, value = """
            INSERT INTO car_brands_branches (car_brand_id, branch_id) VALUES (:brandId, :branchId)
            """)
    void linkToBranch(@Param("brandId") Integer brandId, @Param("branchId") UUID branchId);
}
