package com.hutnyk.carfix.in.search.query;

public record SearchSuggestionsQuery(
        String q,
        //Nullable
        String city,
        //Nullable
        String voivodeship,
        //Nullable
        String country
) {}
