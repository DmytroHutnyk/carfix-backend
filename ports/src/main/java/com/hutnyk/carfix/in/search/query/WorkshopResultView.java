package com.hutnyk.carfix.in.search.query;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record WorkshopResultView(
        UUID branchId,
        String name,
        String streetName,
        String buildingNumber,
        String city,
        BigDecimal latitude,
        BigDecimal longitude,
        //Nullable — present only when the caller sent lat/lng
        Double distanceKm,
        //Nullable pair — null = no reviews yet ("New" on the card)
        BigDecimal rating,
        Integer reviewCount,
        List<MatchedServiceView> matchedServices,
        //IANA zone of the branch; the availability starts below are local to it
        String tz,
        //Nullable — filled only on the availability path
        List<AvailableStartView> nextAvailableStarts
) {
    public WorkshopResultView withNextAvailableStarts(List<AvailableStartView> starts) {
        return new WorkshopResultView(branchId, name, streetName, buildingNumber, city, latitude, longitude,
                distanceKm, rating, reviewCount, matchedServices, tz, starts);
    }
}
