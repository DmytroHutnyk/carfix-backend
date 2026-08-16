package com.hutnyk.carfix.in.branch.query;

import com.hutnyk.carfix.branch.BranchStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OwnerBranchSummaryView(
        UUID branchId,
        String name,
        BranchStatus status,
        String streetName,
        String buildingNumber,
        String city,
        //Nullable pair — null = no reviews yet
        BigDecimal rating,
        Integer reviewCount,
        boolean openNow,
        int bookingsToday,
        int completedToday,
        int employeesOnDutyToday,
        int employeesTotal,
        List<BranchReviewView> latestReviews
) {
}
