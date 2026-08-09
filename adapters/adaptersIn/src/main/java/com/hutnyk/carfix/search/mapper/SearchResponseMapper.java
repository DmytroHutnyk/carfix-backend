package com.hutnyk.carfix.search.mapper;

import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.search.dto.response.CategorySuggestionResponse;
import com.hutnyk.carfix.search.dto.response.MatchedServiceResponse;
import com.hutnyk.carfix.search.dto.response.SearchSuggestionsResponse;
import com.hutnyk.carfix.search.dto.response.ServiceSuggestionResponse;
import com.hutnyk.carfix.search.dto.response.WorkshopResultResponse;
import com.hutnyk.carfix.search.dto.response.WorkshopSearchPageResponse;
import com.hutnyk.carfix.search.dto.response.WorkshopSuggestionResponse;

public class SearchResponseMapper {

    public static SearchSuggestionsResponse toResponse(SearchSuggestionsView view) {
        if (view == null) {
            return null;
        }
        return new SearchSuggestionsResponse(
                view.services().stream()
                        .map(service -> new ServiceSuggestionResponse(service.name(), service.categoryName()))
                        .toList(),
                view.categories().stream()
                        .map(category -> new CategorySuggestionResponse(category.categoryId(), category.name()))
                        .toList(),
                view.workshops().stream()
                        .map(workshop -> new WorkshopSuggestionResponse(workshop.branchId(), workshop.name()))
                        .toList());
    }

    public static WorkshopSearchPageResponse toResponse(WorkshopSearchPage page) {
        if (page == null) {
            return null;
        }
        return new WorkshopSearchPageResponse(
                page.content().stream().map(SearchResponseMapper::toResponse).toList(),
                page.page(), page.size(), page.totalElements(), page.totalPages());
    }

    private static WorkshopResultResponse toResponse(WorkshopResultView view) {
        return new WorkshopResultResponse(
                view.branchId(), view.name(), view.streetName(), view.buildingNumber(), view.city(),
                view.latitude(), view.longitude(), view.distanceKm(),
                view.matchedServices().stream()
                        .map(service -> new MatchedServiceResponse(
                                service.serviceId(), service.name(), service.price(),
                                service.durationMinutes(), service.categoryName()))
                        .toList());
    }
}
