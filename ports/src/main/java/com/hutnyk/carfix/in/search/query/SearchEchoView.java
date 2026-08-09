package com.hutnyk.carfix.in.search.query;

/**
 * Effective normalized filters the search actually ran with; the FE composes the page title from it.
 * All fields nullable
 */
public record SearchEchoView(
        String q,
        String serviceName,
        Integer categoryId,
        String categoryName,
        String city,
        String voivodeship,
        String country
) {}
