package com.hutnyk.carfix.review.dto.response;

import java.util.UUID;

public record ReviewResponse(
        UUID reviewId,
        Integer starsNumber,
        String contents,
        UUID bookingId
) {}
