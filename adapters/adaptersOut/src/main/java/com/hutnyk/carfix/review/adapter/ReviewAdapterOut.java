package com.hutnyk.carfix.review.adapter;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.entity.ReviewEntity;
import com.hutnyk.carfix.review.mapper.ReviewMapper;
import com.hutnyk.carfix.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@PersistenceAdapter
public class ReviewAdapterOut implements ReviewPortOut {

    private final ReviewRepository reviewRepository;
    private final EntityManager em;

    @Override
    public boolean existsByBookingId(BookingId bookingId) {
        return reviewRepository.existsByBookingEntityId(bookingId.id());
    }

    @Override
    public Optional<Review> findByBookingId(BookingId bookingId) {
        return reviewRepository.findByBookingEntityId(bookingId.id())
                .map(ReviewMapper::toDomain);
    }

    @Override
    public Review insert(Review review) {
        /* getReference sets the FK without loading the booking row we do not need. */
        BookingEntity bookingReference = em.getReference(BookingEntity.class, review.getBookingId().id());
        ReviewEntity inserted = reviewRepository.save(ReviewMapper.toEntity(review, bookingReference));
        return ReviewMapper.toDomain(inserted);
    }

    @Override
    public Optional<BranchId> findBranchIdByBookingId(BookingId bookingId) {
        return reviewRepository.findBranchIdByBookingId(bookingId.id()).map(BranchId::of);
    }

    @Override
    public List<Integer> findStarsByBranchId(BranchId branchId) {
        return reviewRepository.findStarsByBranchId(branchId.id());
    }
}
