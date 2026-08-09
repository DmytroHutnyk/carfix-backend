package com.hutnyk.carfix.out.search;

import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.ServiceSuggestionView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSuggestionView;

import java.util.List;
import java.util.Optional;

public interface SearchPortOut {

    List<ServiceSuggestionView> findServiceSuggestions(String q, int limit);

    List<CategorySuggestionView> findCategorySuggestions(String q, int limit);

    List<WorkshopSuggestionView> findWorkshopSuggestions(SearchSuggestionsQuery query, int limit);

    WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, Integer brandId);

    Optional<String> findCategoryName(Integer categoryId);
}
