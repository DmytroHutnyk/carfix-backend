package com.hutnyk.carfix.serviceBay.repository;

import com.hutnyk.carfix.serviceBay.entity.ServiceBayTypeEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ServiceBayTypeRepository extends JpaRepository<ServiceBayTypeEntity, Integer> {

    @Query("select t from ServiceBayTypeEntity t where t.branchEntity is null or t.branchEntity.id = :branchId")
    List<ServiceBayTypeEntity> findAllForBranch(@Param("branchId") UUID branchId);

    @Query("select case when count(t) > 0 then true else false end from ServiceBayTypeEntity t "
            + "where t.id = :typeId and (t.branchEntity is null or t.branchEntity.id = :branchId)")
    boolean existsForBranch(@Param("typeId") Integer typeId, @Param("branchId") UUID branchId);
}
