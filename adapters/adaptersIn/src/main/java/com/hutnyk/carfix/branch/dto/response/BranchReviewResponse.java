package com.hutnyk.carfix.branch.dto.response;

import java.time.Instant;
import java.util.UUID;

public record BranchReviewResponse(
        UUID reviewId,
        Integer starsNumber,
        String contents,
        Instant createdAt,
        String customerName,
        String customerSurname
) {}
