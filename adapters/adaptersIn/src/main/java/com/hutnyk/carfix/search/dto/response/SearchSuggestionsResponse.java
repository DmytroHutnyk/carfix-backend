package com.hutnyk.carfix.search.dto.response;

import java.util.List;

public record SearchSuggestionsResponse(
        List<ServiceSuggestionResponse> services,
        List<CategorySuggestionResponse> categories,
        List<WorkshopSuggestionResponse> workshops
) {}
