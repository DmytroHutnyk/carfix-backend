package com.hutnyk.carfix.in.search.query;

// Effective normalized filters used by both search and page title.
public record SearchEchoView(
        String q,
        String serviceName,
        Integer categoryId,
        String categoryName,
        String city,
        String voivodeship,
        String country,
        AvailabilityWindow availability
) {}
