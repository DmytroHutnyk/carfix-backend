package com.hutnyk.carfix.review;

import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.exception.DomainObjectValidationException;
import com.hutnyk.carfix.exception.ValidationErrorType;
import com.hutnyk.carfix.util.Validator;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

//@With
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class Review {

    @EqualsAndHashCode.Include
    private final ReviewId id;
    private final Integer starsNumber;

    //Nullable
    private final String contents;
    private final BookingId bookingId;

    @Builder
    private Review(ReviewId id, Integer starsNumber, String contents, BookingId bookingId) {
        this.id = Validator.notNull(id, "id");
        this.starsNumber = validateStarsNumber(starsNumber);
        this.contents = contents;
        this.bookingId = Validator.notNull(bookingId, "bookingId");
    }

    public static Review of(ReviewId id, Integer starsNumber, String contents, BookingId bookingId) {
        return Review.builder()
                .id(id)
                .starsNumber(starsNumber)
                .contents(contents)
                .bookingId(bookingId)
                .build();
    }

    private static Integer validateStarsNumber(Integer starsNumber) {
        Validator.notNull(starsNumber, "starsNumber");

        if (starsNumber < 1 || starsNumber > 5) {
            throw new DomainObjectValidationException(ValidationErrorType.VALUE_OUT_OF_RANGE, "starsNumber", starsNumber);
        }

        return starsNumber;
    }
}
