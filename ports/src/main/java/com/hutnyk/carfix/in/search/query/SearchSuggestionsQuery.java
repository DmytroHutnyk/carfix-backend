package com.hutnyk.carfix.in.search.query;

import java.util.UUID;

public record SearchSuggestionsQuery(
        String q,
        //Nullable
        String city,
        //Nullable
        String voivodeship,
        //Nullable
        String country,
        //Nullable
        UUID carProfileId
) {}
