package com.hutnyk.carfix.search.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WorkshopResultResponse(
        UUID branchId,
        String name,
        String streetName,
        String buildingNumber,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        Double distanceKm,
        BigDecimal rating,
        Integer reviewCount,
        List<MatchedServiceResponse> matchedServices
) {}
