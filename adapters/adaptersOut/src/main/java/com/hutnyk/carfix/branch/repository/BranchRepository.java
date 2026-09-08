package com.hutnyk.carfix.branch.repository;

import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("SELECT b.tz FROM BranchEntity b WHERE b.id = :branchId AND b.status = :status")
    Optional<String> findTzByIdAndStatus(
            @Param("branchId") UUID branchId, @Param("status") BranchStatus status);

    @Query("""
            SELECT b FROM BranchEntity b
            JOIN FETCH b.addressEntity a
            JOIN FETCH a.cityEntity
            WHERE b.ownerId = :ownerId
            ORDER BY b.name
            """)
    List<BranchEntity> findAllWithAddressByOwnerId(@Param("ownerId") UUID ownerId);

    @Query("""
            SELECT b FROM BranchEntity b
            JOIN FETCH b.addressEntity a
            JOIN FETCH a.cityEntity c
            JOIN FETCH c.regionEntity r
            JOIN FETCH r.countryEntity
            WHERE b.id = :branchId AND b.ownerId = :ownerId
            """)
    Optional<BranchEntity> findWithLocationByIdAndOwnerId(
            @Param("branchId") UUID branchId, @Param("ownerId") UUID ownerId);
}
