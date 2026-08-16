package com.hutnyk.carfix.search;

import com.hutnyk.carfix.carProfile.exception.CarProfileNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.in.search.SearchPortIn;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.SearchEchoView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.carProfile.CarProfilePortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.out.search.SearchPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.search.exception.InvalidSearchFilterException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationService
public class SearchService implements SearchPortIn {

    private static final int SUGGESTION_LIMIT = 5;
    private static final int MIN_QUERY_LENGTH = 2;
    private static final int MAX_PAGE_SIZE = 50;
    private static final double MAX_RADIUS_KM = 50.0;
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);
    private static final int MAX_AVAILABILITY_CANDIDATES = 100;
    private static final int MAX_AVAILABILITY_DAYS = 7;

    private final SearchPortOut searchPortOut;
    private final CustomerPortOut customerPortOut;
    private final CarProfilePortOut carProfilePortOut;
    private final ServicePortOut servicePortOut;
    private final AvailabilityPortOut availabilityPortOut;
    private final Clock clock;

    /**
     * Only the workshops group is location- and brand-bound.
     */
    @Override
    @Transactional(readOnly = true)
    public SearchSuggestionsView getSuggestions(SearchSuggestionsQuery query, String principalEmail) {
        String q = normalize(query.q());
        if (q == null || q.length() < MIN_QUERY_LENGTH) {
            return SearchSuggestionsView.empty();
        }
        Integer brandId = resolveBrandId(query.carProfileId(), principalEmail);
        SearchSuggestionsQuery workshopQuery = new SearchSuggestionsQuery(
                q, normalize(query.city()), normalize(query.voivodeship()), normalize(query.country()),
                query.carProfileId());

        return new SearchSuggestionsView(
                searchPortOut.findServiceSuggestions(q, SUGGESTION_LIMIT),
                searchPortOut.findCategorySuggestions(q, SUGGESTION_LIMIT),
                searchPortOut.findWorkshopSuggestions(workshopQuery, brandId, SUGGESTION_LIMIT));
    }

    @Override
    @Transactional(readOnly = true)
    public WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, String principalEmail) {
        String q = normalize(query.q());
        String serviceName = normalize(query.serviceName());
        Integer categoryId = query.categoryId();

        int filters = (q != null ? 1 : 0) + (serviceName != null ? 1 : 0) + (categoryId != null ? 1 : 0);
        if (filters > 1) {
            throw new InvalidSearchFilterException("at most one of q, serviceName, categoryId may be provided");
        }
        if (q != null && q.length() < MIN_QUERY_LENGTH) {
            throw new InvalidSearchFilterException("q must be at least " + MIN_QUERY_LENGTH + " characters");
        }

        String city = normalize(query.city());
        String voivodeship = normalize(query.voivodeship());
        String country = normalize(query.country());
        validateGeo(query);
        validatePaging(query);

        boolean hasCoordinates = query.lat() != null && query.lng() != null;
        String sort = resolveSort(query.sort(), hasCoordinates);

        Integer brandId = resolveBrandId(query.carProfileId(), principalEmail);

        AvailabilityWindow availability = validateAvailability(query.availability(), serviceName);

        WorkshopSearchQuery normalized = new WorkshopSearchQuery(
                q, serviceName, categoryId,
                city, voivodeship, country,
                query.lat(), query.lng(), query.radiusKm(),
                query.carProfileId(), query.page(), query.size(),
                sort, query.pinnedBranchId(), availability);

        String categoryName = categoryId != null
                ? searchPortOut.findCategoryName(categoryId).orElse(null)
                : null;
        SearchEchoView echo = new SearchEchoView(
                q, serviceName, categoryId, categoryName, city, voivodeship, country, availability);

        if (availability == null) {
            return searchPortOut.searchWorkshops(normalized, brandId).withEcho(echo);
        }
        List<WorkshopResultView> candidates =
                searchPortOut.findAvailabilityCandidates(normalized, brandId, MAX_AVAILABILITY_CANDIDATES);
        List<WorkshopResultView> available =
                new SearchAvailabilityFilter(servicePortOut, availabilityPortOut, clock).filter(candidates, availability);
        return page(available, query.page(), query.size()).withEcho(echo);
    }

    /**
     * The only place the availability precondition is enforced; SearchAvailabilityFilter relies on it.
     */
    private static AvailabilityWindow validateAvailability(AvailabilityWindow window, String serviceName) {
        if (window == null) {
            return null;
        }
        boolean any = window.from() != null || window.to() != null || window.timeFrom() != null || window.timeTo() != null;
        if (!any) {
            return null;
        }
        if (serviceName == null) {
            throw new InvalidSearchFilterException("availability filter requires serviceName");
        }
        if (window.from() == null || window.to() == null) {
            throw new InvalidSearchFilterException("from and to must be given together");
        }
        if (window.to().isBefore(window.from())) {
            throw new InvalidSearchFilterException("to must not be before from");
        }
        if (ChronoUnit.DAYS.between(window.from(), window.to()) + 1 > MAX_AVAILABILITY_DAYS) {
            throw new InvalidSearchFilterException("availability range must not exceed " + MAX_AVAILABILITY_DAYS + " days");
        }
        if (window.timeFrom() != null && window.timeTo() != null && !window.timeFrom().isBefore(window.timeTo())) {
            throw new InvalidSearchFilterException("timeFrom must be before timeTo");
        }
        return window;
    }

    /**
     * The availability path pages after filtering, so the totals count only branches that can take the job.
     */
    private static WorkshopSearchPage page(List<WorkshopResultView> all, int page, int size) {
        int fromIndex = (int) Math.min((long) page * size, all.size());
        int toIndex = Math.min(fromIndex + size, all.size());
        int totalPages = (int) Math.ceil((double) all.size() / size);
        return new WorkshopSearchPage(all.subList(fromIndex, toIndex), page, size, all.size(), totalPages, null);
    }

    /**
     * The order is a fact of the request, so it is resolved once here and the adapter
     * only reads it. Absent = distance when we have a centre to measure from, else name.
     */
    private static String resolveSort(String rawSort, boolean hasCoordinates) {
        String sort = normalize(rawSort);
        if (sort == null) {
            return hasCoordinates ? WorkshopSearchQuery.SORT_DISTANCE : WorkshopSearchQuery.SORT_NAME;
        }
        String lower = sort.toLowerCase();
        if (WorkshopSearchQuery.SORT_NAME.equals(lower)) {
            return WorkshopSearchQuery.SORT_NAME;
        }
        if (WorkshopSearchQuery.SORT_DISTANCE.equals(lower)) {
            if (!hasCoordinates) {
                throw new InvalidSearchFilterException("sort=distance requires lat and lng");
            }
            return WorkshopSearchQuery.SORT_DISTANCE;
        }
        throw new InvalidSearchFilterException("sort must be one of: "
                + WorkshopSearchQuery.SORT_DISTANCE + ", " + WorkshopSearchQuery.SORT_NAME);
    }

    private static void validateGeo(WorkshopSearchQuery query) {
        if (query.lat() == null && query.lng() == null) {
            if (query.radiusKm() != null) {
                throw new InvalidSearchFilterException("radiusKm requires lat and lng");
            }
            return;
        }
        if (query.lat() == null || query.lng() == null) {
            throw new InvalidSearchFilterException("lat and lng must be provided together");
        }
        if (query.lat().abs().compareTo(MAX_LATITUDE) > 0) {
            throw new InvalidSearchFilterException("lat must be between -90 and 90");
        }
        if (query.lng().abs().compareTo(MAX_LONGITUDE) > 0) {
            throw new InvalidSearchFilterException("lng must be between -180 and 180");
        }
        if (query.radiusKm() != null && (query.radiusKm() <= 0 || query.radiusKm() > MAX_RADIUS_KM)) {
            throw new InvalidSearchFilterException("radiusKm must be between 0 and " + MAX_RADIUS_KM);
        }
    }

    private static void validatePaging(WorkshopSearchQuery query) {
        if (query.page() < 0) {
            throw new InvalidSearchFilterException("page must not be negative");
        }
        if (query.size() < 1 || query.size() > MAX_PAGE_SIZE) {
            throw new InvalidSearchFilterException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    private Integer resolveBrandId(UUID carProfileId, String principalEmail) {
        if (carProfileId == null) {
            return null;
        }
        if (principalEmail == null) {
            throw new CarProfileNotFoundException(carProfileId);
        }
        Customer customer = customerPortOut.loadCustomerByUsername(principalEmail);
        UUID customerId = customer.getUser().getId().id();
        return carProfilePortOut.findByIdAndCustomerId(carProfileId, customerId)
                .orElseThrow(() -> new CarProfileNotFoundException(carProfileId))
                .brandId();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
