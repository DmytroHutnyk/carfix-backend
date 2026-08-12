package com.hutnyk.carfix.in.branch.query;

import java.time.Instant;
import java.util.UUID;

public record BranchReviewView(
        UUID reviewId,
        Integer starsNumber,
        //Nullable
        String contents,
        Instant createdAt,
        String customerName,
        String customerSurname
) {}
