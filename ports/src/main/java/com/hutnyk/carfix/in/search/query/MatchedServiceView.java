package com.hutnyk.carfix.in.search.query;

import java.math.BigDecimal;

public record MatchedServiceView(
        Integer serviceId,
        String name,
        BigDecimal price,
        Short durationMinutes,
        String categoryName
) {}
