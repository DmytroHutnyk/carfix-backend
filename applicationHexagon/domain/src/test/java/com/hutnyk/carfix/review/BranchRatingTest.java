package com.hutnyk.carfix.review;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class BranchRatingTest {

    @Test
    void mean_is_rounded_to_one_decimal() {
        List<Integer> stars = List.of(5, 4, 5);

        BranchRating rating = BranchRating.of(stars);

        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.7"));
        assertThat(rating.count()).isEqualTo(3);
    }

    @Test
    void mean_rounds_half_up() {
        List<Integer> stars = List.of(4, 5);

        BranchRating rating = BranchRating.of(stars);

        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    @Test
    void a_single_review_is_the_rating() {
        BranchRating rating = BranchRating.of(List.of(4));

        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.0"));
        assertThat(rating.count()).isEqualTo(1);
    }

    @Test
    void no_reviews_is_NONE_not_zero() {
        BranchRating rating = BranchRating.of(List.of());

        assertThat(rating).isEqualTo(BranchRating.NONE);
        assertThat(rating.average()).isNull();
        assertThat(rating.count()).isZero();
    }
}
