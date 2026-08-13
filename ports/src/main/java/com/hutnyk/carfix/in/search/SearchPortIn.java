package com.hutnyk.carfix.in.search;

import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;

public interface SearchPortIn {

    SearchSuggestionsView getSuggestions(SearchSuggestionsQuery query, String principalEmail);

    WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, String principalEmail);
}
