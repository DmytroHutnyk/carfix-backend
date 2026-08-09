package com.hutnyk.carfix.in.search;

import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;

public interface SearchPortIn {

    SearchSuggestionsView getSuggestions(SearchSuggestionsQuery query);

    /**
     * @param principalEmail the authenticated username, or {@code null} for anonymous callers
     */
    WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, String principalEmail);
}
