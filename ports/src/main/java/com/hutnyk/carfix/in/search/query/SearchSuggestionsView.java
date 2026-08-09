package com.hutnyk.carfix.in.search.query;

import java.util.List;

public record SearchSuggestionsView(
        List<ServiceSuggestionView> services,
        List<CategorySuggestionView> categories,
        List<WorkshopSuggestionView> workshops
) {
    public static SearchSuggestionsView empty() {
        return new SearchSuggestionsView(List.of(), List.of(), List.of());
    }
}
