package com.hutnyk.carfix.branch.repository;

import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<BranchEntity, UUID> {

    @Query("""
            SELECT b FROM BranchEntity b
            JOIN FETCH b.addressEntity a
            JOIN FETCH a.cityEntity
            WHERE b.id = :branchId AND b.status = :status
            """)
    Optional<BranchEntity> findWithAddressByIdAndStatus(
            @Param("branchId") UUID branchId, @Param("status") BranchStatus status);

    boolean existsByIdAndStatus(UUID id, BranchStatus status);
}
