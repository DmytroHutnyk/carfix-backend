package com.hutnyk.carfix.in.branch.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BranchView(
        UUID branchId,
        String name,
        String phoneNumber,
        String email,
        //Nullable
        String description,
        //Nullable
        String cancellationPolicy,
        //Nullable pair — null = no reviews yet ("New")
        BigDecimal rating,
        Integer reviewCount,
        String streetName,
        String buildingNumber,
        String city,
        //Nullable pair — address may lack coordinates
        BigDecimal latitude,
        BigDecimal longitude,
        //Nullable
        String googlePlaceId,
        String tz,
        List<BranchBrandView> brands,
        List<BranchOpeningHoursView> openingHours,
        List<BranchServiceCategoryView> serviceCategories
) {}
