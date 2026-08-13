package com.hutnyk.carfix.review.repository;

import com.hutnyk.carfix.review.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID> {

    Optional<ReviewEntity> findByBookingEntityId(UUID bookingId);

    boolean existsByBookingEntityId(UUID bookingId);

    /**
     * Raw stars, not an average: the mean is a business rule and lives in the domain({@code BranchRating}).
     */
    @Query("""
            SELECT r.starsNumber
            FROM ReviewEntity r
            JOIN r.bookingEntity b
            WHERE b.branchEntity.id = :branchId
            """)
    List<Integer> findStarsByBranchId(@Param("branchId") UUID branchId);

    @Query("""
            SELECT b.branchEntity.id
            FROM BookingEntity b
            WHERE b.id = :bookingId
            """)
    Optional<UUID> findBranchIdByBookingId(@Param("bookingId") UUID bookingId);

    @Query(value = """
            SELECT r, u.name, u.surname FROM ReviewEntity r
            JOIN r.bookingEntity b
            JOIN b.carProfileEntity cp
            JOIN cp.customerEntity c
            JOIN c.userEntity u
            WHERE b.branchEntity.id = :branchId
            """,
            countQuery = """
            SELECT COUNT(r) FROM ReviewEntity r
            WHERE r.bookingEntity.branchEntity.id = :branchId
            """)
    Page<Object[]> findReviewRowsByBranchId(@Param("branchId") UUID branchId, Pageable pageable);
}
