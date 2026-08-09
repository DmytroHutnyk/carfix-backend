package com.hutnyk.carfix.review.repository;

import com.hutnyk.carfix.review.entity.ReviewEntity;
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
     * Raw stars, not an average: the mean is a business rule and lives in the domain
     * ({@code BranchRating}). A branch has tens of reviews, not millions, so there is no
     * performance case for pushing the AVG into SQL and duplicating the rule.
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
}
