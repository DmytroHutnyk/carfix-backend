package com.hutnyk.carfix.search.dto.response;

public record SearchEchoResponse(
        String q,
        String serviceName,
        Integer categoryId,
        String categoryName,
        String city,
        String voivodeship,
        String country
) {}
