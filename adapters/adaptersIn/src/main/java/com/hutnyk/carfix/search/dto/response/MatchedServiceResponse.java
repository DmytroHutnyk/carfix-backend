package com.hutnyk.carfix.search.dto.response;

import java.math.BigDecimal;

public record MatchedServiceResponse(
        Integer serviceId,
        String name,
        BigDecimal price,
        Short durationMinutes,
        String categoryName
) {}
