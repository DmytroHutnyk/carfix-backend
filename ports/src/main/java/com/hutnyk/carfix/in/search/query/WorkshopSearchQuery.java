package com.hutnyk.carfix.in.search.query;

import java.math.BigDecimal;
import java.util.UUID;

public record WorkshopSearchQuery(
        //Nullable
        String q,
        //Nullable
        String serviceName,
        //Nullable
        Integer categoryId,

        //Nullable
        String city,
        //Nullable
        String voivodeship,
        //Nullable — 2-letter ISO code, not a country name
        String country,

        //Nullable
        BigDecimal lat,
        //Nullable
        BigDecimal lng,
        //Nullable — requires lat/lng; present = bbox+country replace the city/voivodeship filters
        Double radiusKm,

        //Nullable
        UUID carProfileId,
        int page,
        int size,

        //Nullable on the way in; never null after SearchService normalizes it
        String sort,
        //Nullable — this branch is kept in the result set and ordered first
        UUID pinnedBranchId,

        //Nullable — null after SearchService normalizes an all-null window
        AvailabilityWindow availability
) {
    public static final String SORT_DISTANCE = "distance";
    public static final String SORT_NAME = "name";
}
