package com.hutnyk.carfix.branch.dto.response;

import com.hutnyk.carfix.branch.BranchStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OwnerBranchSummaryResponse(
        UUID branchId,
        String name,
        BranchStatus status,
        String streetName,
        String buildingNumber,
        String city,
        BigDecimal rating,
        Integer reviewCount,
        boolean openNow,
        int bookingsToday,
        int completedToday,
        int employeesOnDutyToday,
        int employeesTotal,
        List<BranchReviewResponse> latestReviews
) {
}
