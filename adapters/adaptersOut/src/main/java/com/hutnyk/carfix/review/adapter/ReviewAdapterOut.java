package com.hutnyk.carfix.review.adapter;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.BranchReviewsPage;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import com.hutnyk.carfix.out.review.ReviewPortOut;
import com.hutnyk.carfix.review.Review;
import com.hutnyk.carfix.review.entity.ReviewEntity;
import com.hutnyk.carfix.review.mapper.ReviewMapper;
import com.hutnyk.carfix.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    public void deleteByCustomerId(UUID customerId) {
        reviewRepository.deleteByCustomerId(customerId);
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

    @Override
    public BranchReviewsPage findReviewsPage(BranchReviewsQuery query) {
        Page<Object[]> page = reviewRepository.findReviewRowsByBranchId(
                query.branchId(),
                PageRequest.of(query.page(), query.size(), sortFor(query.sort())));
        List<BranchReviewView> content = page.getContent().stream()
                .map(row -> ReviewMapper.toBranchReviewView(
                        (ReviewEntity) row[0], (String) row[1], (String) row[2]))
                .toList();
        return new BranchReviewsPage(
                content, query.page(), query.size(), page.getTotalElements(), page.getTotalPages());
    }

    private static Sort sortFor(String sort) {
        return switch (sort) {
            case BranchReviewsQuery.SORT_HIGHEST ->
                    Sort.by(Sort.Order.desc("starsNumber"), Sort.Order.desc("createdAt"));
            case BranchReviewsQuery.SORT_LOWEST ->
                    Sort.by(Sort.Order.asc("starsNumber"), Sort.Order.desc("createdAt"));
            default -> Sort.by(Sort.Order.desc("createdAt"));
        };
    }
}
