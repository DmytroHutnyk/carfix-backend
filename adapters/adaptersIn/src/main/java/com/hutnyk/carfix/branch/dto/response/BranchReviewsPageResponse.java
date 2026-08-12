package com.hutnyk.carfix.branch.dto.response;

import java.util.List;

public record BranchReviewsPageResponse(
        List<BranchReviewResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
