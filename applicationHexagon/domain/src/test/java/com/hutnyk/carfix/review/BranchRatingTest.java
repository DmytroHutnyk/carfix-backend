package com.hutnyk.carfix.review;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

public class BranchRatingTest {

    @Test
    void mean_is_rounded_to_one_decimal() {
        //given
        List<Integer> stars = List.of(5, 4, 5);

        //when
        BranchRating rating = BranchRating.of(stars);

        //then
        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.7"));
        assertThat(rating.count()).isEqualTo(3);
    }

    @Test
    void mean_rounds_half_up() {
        //given
        List<Integer> stars = List.of(4, 5);

        //when
        BranchRating rating = BranchRating.of(stars);

        //then
        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.5"));
    }

    @Test
    void a_single_review_is_the_rating() {
        //given / when
        BranchRating rating = BranchRating.of(List.of(4));

        //then
        assertThat(rating.average()).isEqualByComparingTo(new BigDecimal("4.0"));
        assertThat(rating.count()).isEqualTo(1);
    }

    @Test
    void no_reviews_is_NONE_not_zero() {
        //given / when
        BranchRating rating = BranchRating.of(List.of());

        //then
        assertThat(rating).isEqualTo(BranchRating.NONE);
        assertThat(rating.average()).isNull();
        assertThat(rating.count()).isZero();
    }
}
