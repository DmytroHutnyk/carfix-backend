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

        //Nullable — at least one of city/voivodeship/country must be present
        String city,
        //Nullable
        String voivodeship,
        //Nullable
        String country,

        //Nullable
        BigDecimal lat,
        //Nullable
        BigDecimal lng,
        //Nullable — requires lat/lng; absent = no radius cut
        Double radiusKm,

        //Nullable
        UUID carProfileId,
        int page,
        int size
) {}
