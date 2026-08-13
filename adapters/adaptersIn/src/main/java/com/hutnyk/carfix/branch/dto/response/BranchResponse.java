package com.hutnyk.carfix.branch.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BranchResponse(
        UUID branchId,
        String name,
        String phoneNumber,
        String email,
        String description,
        String cancellationPolicy,
        BigDecimal rating,
        Integer reviewCount,
        String streetName,
        String buildingNumber,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        String googlePlaceId,
        String tz,
        List<BranchBrandResponse> brands,
        List<BranchOpeningHoursResponse> openingHours,
        List<BranchServiceCategoryResponse> serviceCategories
) {}
