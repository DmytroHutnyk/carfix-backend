package com.hutnyk.carfix.review.entity;

import com.hutnyk.carfix.booking.entity.BookingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "reviews")
@Entity
public class ReviewEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "review_id", nullable = false)
    private UUID id;

    @Column(name = "stars_number", nullable = false)
    private Integer starsNumber;

    @Column(name = "contents")
    private String contents;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    // uq_reviews_booking_id makes this one-to-one, but the owning side is still a plain FK column.
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private BookingEntity bookingEntity;
}
