package com.hutnyk.carfix.search.controller;

import com.hutnyk.carfix.in.search.SearchPortIn;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.search.dto.response.SearchSuggestionsResponse;
import com.hutnyk.carfix.search.dto.response.WorkshopSearchPageResponse;
import com.hutnyk.carfix.search.mapper.SearchResponseMapper;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchPortIn searchPortIn;

    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionsResponse> getSuggestions(
            @RequestParam(name = "q") @Size(max = 100) String q,
            @RequestParam(name = "city", required = false) @Size(max = 100) String city,
            @RequestParam(name = "voivodeship", required = false) @Size(max = 100) String voivodeship,
            @RequestParam(name = "country", required = false) @Size(max = 100) String country) {
        SearchSuggestionsQuery query = new SearchSuggestionsQuery(q, city, voivodeship, country);
        return ResponseEntity.ok(SearchResponseMapper.toResponse(searchPortIn.getSuggestions(query)));
    }

    @GetMapping("/workshops")
    public ResponseEntity<WorkshopSearchPageResponse> searchWorkshops(
            @RequestParam(name = "q", required = false) @Size(max = 100) String q,
            @RequestParam(name = "serviceName", required = false) @Size(max = 100) String serviceName,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "city", required = false) @Size(max = 100) String city,
            @RequestParam(name = "voivodeship", required = false) @Size(max = 100) String voivodeship,
            @RequestParam(name = "country", required = false) @Size(max = 100) String country,
            @RequestParam(name = "lat", required = false) @DecimalMin("-90") @DecimalMax("90") BigDecimal lat,
            @RequestParam(name = "lng", required = false) @DecimalMin("-180") @DecimalMax("180") BigDecimal lng,
            @RequestParam(name = "radiusKm", required = false) @DecimalMin("0.1") @DecimalMax("50") Double radiusKm,
            @RequestParam(name = "carProfileId", required = false) UUID carProfileId,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(name = "sort", required = false) @Size(max = 20) String sort,
            @RequestParam(name = "pinnedBranchId", required = false) UUID pinnedBranchId,
            @AuthenticationPrincipal UserDetails principal) {
        WorkshopSearchQuery query = new WorkshopSearchQuery(
                q, serviceName, categoryId, city, voivodeship, country,
                lat, lng, radiusKm, carProfileId, page, size,
                sort, pinnedBranchId);
        String principalEmail = principal != null ? principal.getUsername() : null;
        return ResponseEntity.ok(SearchResponseMapper.toResponse(
                searchPortIn.searchWorkshops(query, principalEmail)));
    }
}
