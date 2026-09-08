package com.hutnyk.carfix.search.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.CategorySuggestionView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
import com.hutnyk.carfix.in.search.query.SearchSuggestionsQuery;
import com.hutnyk.carfix.in.search.query.ServiceSuggestionView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.in.search.query.WorkshopSearchPage;
import com.hutnyk.carfix.in.search.query.WorkshopSearchQuery;
import com.hutnyk.carfix.in.search.query.WorkshopSuggestionView;
import com.hutnyk.carfix.out.search.SearchPortOut;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class SearchAdapterOut implements SearchPortOut {

    private static final double MIN_WORD_SIMILARITY = 0.35;

    private static final double KM_PER_DEGREE = 111.32;
    private static final int MAX_MATCHED_SERVICES = 3;
    private static final int MINUTES_PER_DAY = 24 * 60;

    private final EntityManager em;

    private static final String SERVICE_SUGGESTIONS_SQL = """
            SELECT s.name, sc.name AS category_name
            FROM services s
            JOIN service_categories sc ON sc.service_category_id = s.service_category_id
            JOIN branches b ON b.branch_id = s.branch_id
            WHERE s.status = 'ACTIVE' AND b.status = 'ACTIVE'
              AND (word_similarity(:q, s.name) >= :minSimilarity OR s.name ILIKE '%' || :q || '%')
            GROUP BY s.name, sc.name
            ORDER BY MAX(word_similarity(:q, s.name)) DESC, s.name
            LIMIT :limit
            """;

    private static final String CATEGORY_SUGGESTIONS_SQL = """
            SELECT sc.service_category_id, sc.name
            FROM service_categories sc
            WHERE word_similarity(:q, sc.name) >= :minSimilarity OR sc.name ILIKE '%' || :q || '%'
            ORDER BY word_similarity(:q, sc.name) DESC, sc.name
            LIMIT :limit
            """;

    private static final String WORKSHOP_SUGGESTIONS_SELECT = """
            SELECT b.branch_id, b.name
            FROM branches b
            """;

    private static final String WORKSHOP_SUGGESTIONS_MATCH = """
            WHERE b.status = 'ACTIVE'
              AND (word_similarity(:q, b.name) >= :minSimilarity OR b.name ILIKE '%' || :q || '%')
            """;

    private static final String WORKSHOP_SUGGESTIONS_ORDER = """
            ORDER BY word_similarity(:q, b.name) DESC, b.name
            LIMIT :limit
            """;

    // Suggestions and results share one administrative-location chain.
    private static final String LOCATION_JOINS = """
            JOIN addresses a ON a.address_id = b.address_id
            JOIN cities c ON c.city_id = a.city_id
            JOIN regions r ON r.region_id = c.region_id
            JOIN countries co ON co.iso = r.countries_iso
            """;

    private static final String CITY_FILTER = "  AND c.name ILIKE :city\n";
    private static final String VOIVODESHIP_FILTER = "  AND r.name ILIKE :voivodeship\n";
    /* ISO, not name: the region selector and every URL carry the 2-letter code, and
       country names are localized while the code is not. */
    private static final String COUNTRY_FILTER = "  AND co.iso = :country\n";

    private static final String BBOX_FILTER = """
              AND a.latitude  BETWEEN :latMin AND :latMax
              AND a.longitude BETWEEN :lngMin AND :lngMax
            """;

    private static final String MATCH_BY_SERVICE_NAME = "s.name ILIKE :serviceName";
    private static final String MATCH_BY_CATEGORY = "s.service_category_id = :categoryId";
    private static final String MATCH_BY_TEXT = """
            (word_similarity(:q, s.name) >= :minSimilarity OR s.name ILIKE '%' || :q || '%' \
            OR word_similarity(:q, sc.name) >= :minSimilarity OR sc.name ILIKE '%' || :q || '%')""";

    private static final String TEXT_RANK_ORDER =
            "GREATEST(word_similarity(:q, s.name), word_similarity(:q, sc.name)) DESC, s.name";
    private static final String NAME_RANK_ORDER = "s.name";

    private static final String BRANCH_FILTER_BASE = """
            FROM branches b
            JOIN addresses a ON a.address_id = b.address_id
            JOIN cities c ON c.city_id = a.city_id
            JOIN regions r ON r.region_id = c.region_id
            JOIN countries co ON co.iso = r.countries_iso
            WHERE b.status = 'ACTIVE'
            %s
            """;

    private static final String SERVICE_MATCH_PREDICATE = """
            EXISTS (
                SELECT 1 FROM services s
                JOIN service_categories sc ON sc.service_category_id = s.service_category_id
                WHERE s.branch_id = b.branch_id AND s.status = 'ACTIVE' AND %s
            )""";

    /* Free text also matches the workshop's own name — a customer typing "Kowalski" means
       the workshop, not a service. Used raw (never through String.formatted), so a single %. */
    private static final String BRANCH_NAME_PREDICATE =
            "word_similarity(:q, b.name) >= :minSimilarity OR b.name ILIKE '%' || :q || '%'";

    private static final String PINNED_PREDICATE = "b.branch_id = :pinnedBranchId";

    private static final String BRAND_FILTER = """
              AND EXISTS (
                  SELECT 1 FROM car_brands_branches cb
                  WHERE cb.branch_id = b.branch_id AND cb.car_brand_id = :brandId
              )
            """;

    /* Coarse pre-filter: some ACTIVE bay of a type the matched service accepts has an availability row
       on a day of the range overlapping that day's window; staff/equipment are settled in Java (layer 2). */
    private static final String AVAILABILITY_FILTER = """
              AND EXISTS (
                  SELECT 1 FROM services s2
                  JOIN services_service_bay_types sbt ON sbt.service_id = s2.service_id
                  JOIN service_bays sb ON sb.branch_id = b.branch_id
                                      AND sb.service_bay_type_id = sbt.service_bay_type_id
                                      AND sb.status = 'ACTIVE'
                  JOIN service_bays_availability sba ON sba.service_bay_id = sb.service_bay_id
                  WHERE s2.branch_id = b.branch_id AND s2.status = 'ACTIVE' AND s2.name ILIKE :serviceName
                    AND sba.date BETWEEN :availFrom AND :availTo
                    AND sba.available_time && tsrange(sba.date + (:winFromMin * INTERVAL '1 minute'),
                                                      sba.date + (:winToMin * INTERVAL '1 minute'), '[)')
              )
            """;

    private static final String PAGE_SELECT = """
            SELECT b.branch_id, b.name, a.street_name, a.building_number, c.name AS city_name,
                   a.latitude, a.longitude, b.rating, b.review_count, b.tz
            """;

    private static final String DISTANCE_ORDER_EXPR =
            "(power(a.latitude - :lat, 2) + power((a.longitude - :lng) * :lngScale, 2))";
    private static final String NAME_ORDER_EXPR = "b.name";
    /* Boolean DESC puts the match first; a stable order means the pinned row appears on page 0 only. */
    private static final String PINNED_ORDER_EXPR = "(b.branch_id = :pinnedBranchId) DESC";
    private static final String PAGE_LIMIT = "\nLIMIT :size OFFSET :offset\n";
    private static final String CANDIDATE_LIMIT = "\nLIMIT :limit\n";

    private static final String CATEGORY_NAME_SQL =
            "SELECT name FROM service_categories WHERE service_category_id = :categoryId";

    private static final String PREVIEW_SQL = """
            SELECT branch_id, service_id, name, price, duration_minutes, category_name FROM (
                SELECT s.branch_id, s.service_id, s.name, s.price, s.duration_minutes,
                       sc.name AS category_name,
                       ROW_NUMBER() OVER (PARTITION BY s.branch_id ORDER BY %s) AS rn
                FROM services s
                JOIN service_categories sc ON sc.service_category_id = s.service_category_id
                WHERE s.branch_id IN (:branchIds) AND s.status = 'ACTIVE' AND %s
            ) ranked WHERE rn <= :maxMatched
            """;

    @Override
    @SuppressWarnings("unchecked")
    public List<ServiceSuggestionView> findServiceSuggestions(String q, int limit) {
        List<Object[]> rows = em.createNativeQuery(SERVICE_SUGGESTIONS_SQL)
                .setParameter("q", q)
                .setParameter("minSimilarity", MIN_WORD_SIMILARITY)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> new ServiceSuggestionView((String) row[0], (String) row[1]))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<CategorySuggestionView> findCategorySuggestions(String q, int limit) {
        List<Object[]> rows = em.createNativeQuery(CATEGORY_SUGGESTIONS_SQL)
                .setParameter("q", q)
                .setParameter("minSimilarity", MIN_WORD_SIMILARITY)
                .setParameter("limit", limit)
                .getResultList();
        return rows.stream()
                .map(row -> new CategorySuggestionView(((Number) row[0]).intValue(), (String) row[1]))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkshopSuggestionView> findWorkshopSuggestions(
            SearchSuggestionsQuery query, Integer brandId, int limit) {
        String locationFilter = locationFilter(query.city(), query.voivodeship(), query.country());
        String sql = WORKSHOP_SUGGESTIONS_SELECT
                + (locationFilter.isEmpty() ? "" : LOCATION_JOINS)
                + WORKSHOP_SUGGESTIONS_MATCH
                + locationFilter
                + (brandId != null ? BRAND_FILTER : "")
                + WORKSHOP_SUGGESTIONS_ORDER;

        Query nativeQuery = em.createNativeQuery(sql)
                .setParameter("q", query.q())
                .setParameter("minSimilarity", MIN_WORD_SIMILARITY)
                .setParameter("limit", limit);
        bindLocationParams(nativeQuery, query.city(), query.voivodeship(), query.country());
        if (brandId != null) {
            nativeQuery.setParameter("brandId", brandId);
        }

        List<Object[]> rows = nativeQuery.getResultList();
        return rows.stream()
                .map(row -> new WorkshopSuggestionView((UUID) row[0], (String) row[1]))
                .toList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public WorkshopSearchPage searchWorkshops(WorkshopSearchQuery query, Integer brandId) {
        boolean hasCoordinates = query.lat() != null && query.lng() != null;
        boolean sortByDistance = WorkshopSearchQuery.SORT_DISTANCE.equals(query.sort()) && hasCoordinates;
        boolean pinned = query.pinnedBranchId() != null;

        String filter = filter(query, brandId);

        // :pinnedBranchId may appear only in ORDER BY; binding an absent SQL parameter throws.
        boolean filterHasPinned = pinned && !matchFilter(query).isEmpty();

        Query countQuery = em.createNativeQuery("SELECT COUNT(*) " + filter);
        bindFilterParams(countQuery, query, brandId);
        if (filterHasPinned) {
            countQuery.setParameter("pinnedBranchId", query.pinnedBranchId());
        }
        long total = ((Number) countQuery.getSingleResult()).longValue();

        if (total == 0) {
            return WorkshopSearchPage.empty(query.page(), query.size());
        }

        Query pageQuery = em.createNativeQuery(
                PAGE_SELECT + filter + orderBy(pinned, sortByDistance) + PAGE_LIMIT);
        bindFilterParams(pageQuery, query, brandId);
        bindOrderParams(pageQuery, query, pinned, sortByDistance);
        pageQuery.setParameter("size", query.size());
        pageQuery.setParameter("offset", (long) query.page() * query.size());
        List<Object[]> branchRows = pageQuery.getResultList();

        List<WorkshopResultView> content = toViews(branchRows, query, hasCoordinates);
        int totalPages = (int) Math.ceil((double) total / query.size());
        return new WorkshopSearchPage(content, query.page(), query.size(), total, totalPages, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<WorkshopResultView> findAvailabilityCandidates(
            WorkshopSearchQuery query, Integer brandId, int limit) {
        boolean hasCoordinates = query.lat() != null && query.lng() != null;
        boolean sortByDistance = WorkshopSearchQuery.SORT_DISTANCE.equals(query.sort()) && hasCoordinates;
        boolean pinned = query.pinnedBranchId() != null;

        Query candidateQuery = em.createNativeQuery(
                PAGE_SELECT + filter(query, brandId) + orderBy(pinned, sortByDistance) + CANDIDATE_LIMIT);
        bindFilterParams(candidateQuery, query, brandId);
        bindOrderParams(candidateQuery, query, pinned, sortByDistance);
        candidateQuery.setParameter("limit", limit);
        List<Object[]> rows = candidateQuery.getResultList();
        return toViews(rows, query, hasCoordinates);
    }

    @Override
    public Optional<String> findCategoryName(Integer categoryId) {
        List<?> rows = em.createNativeQuery(CATEGORY_NAME_SQL)
                .setParameter("categoryId", categoryId)
                .getResultList();
        return rows.isEmpty() ? Optional.empty() : Optional.of((String) rows.get(0));
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, List<MatchedServiceView>> findMatchedServices(WorkshopSearchQuery query, List<UUID> branchIds) {
        if (branchIds.isEmpty()) {
            return Map.of();
        }
        String rankOrder = query.q() != null ? TEXT_RANK_ORDER : NAME_RANK_ORDER;
        Query previewQuery = em.createNativeQuery(PREVIEW_SQL.formatted(rankOrder, matchFragment(query)));
        bindMatchParams(previewQuery, query);
        previewQuery.setParameter("branchIds", branchIds);
        previewQuery.setParameter("maxMatched", MAX_MATCHED_SERVICES);
        List<Object[]> rows = previewQuery.getResultList();

        return rows.stream().collect(Collectors.groupingBy(
                row -> (UUID) row[0],
                LinkedHashMap::new,
                Collectors.mapping(row -> new MatchedServiceView(
                        ((Number) row[1]).intValue(), (String) row[2], (BigDecimal) row[3],
                        ((Number) row[4]).shortValue(), (String) row[5]), Collectors.toList())));
    }

    private List<WorkshopResultView> toViews(
            List<Object[]> branchRows, WorkshopSearchQuery query, boolean hasCoordinates) {
        List<UUID> branchIds = branchRows.stream().map(row -> (UUID) row[0]).toList();
        Map<UUID, List<MatchedServiceView>> matchedServices = findMatchedServices(query, branchIds);
        return branchRows.stream()
                .map(row -> {
                    UUID branchId = (UUID) row[0];
                    BigDecimal branchLat = (BigDecimal) row[5];
                    BigDecimal branchLng = (BigDecimal) row[6];
                    return new WorkshopResultView(
                            branchId, (String) row[1], (String) row[2], (String) row[3], (String) row[4],
                            branchLat, branchLng,
                            hasCoordinates ? distanceKm(query.lat(), query.lng(), branchLat, branchLng) : null,
                            (BigDecimal) row[7],
                            row[8] != null ? ((Number) row[8]).intValue() : null,
                            matchedServices.getOrDefault(branchId, List.of()),
                            (String) row[9],
                            null);
                })
                .toList();
    }

    private static String locationFilter(String city, String voivodeship, String country) {
        StringBuilder filter = new StringBuilder();
        if (city != null) {
            filter.append(CITY_FILTER);
        }
        if (voivodeship != null) {
            filter.append(VOIVODESHIP_FILTER);
        }
        if (country != null) {
            filter.append(COUNTRY_FILTER);
        }
        return filter.toString();
    }

    private static void bindLocationParams(Query nativeQuery, String city, String voivodeship, String country) {
        if (city != null) {
            nativeQuery.setParameter("city", city);
        }
        if (voivodeship != null) {
            nativeQuery.setParameter("voivodeship", voivodeship);
        }
        if (country != null) {
            nativeQuery.setParameter("country", country);
        }
    }

    private static boolean hasTextFilter(WorkshopSearchQuery query) {
        return query.q() != null || query.serviceName() != null || query.categoryId() != null;
    }

    private static String matchFragment(WorkshopSearchQuery query) {
        if (query.serviceName() != null) {
            return MATCH_BY_SERVICE_NAME;
        }
        if (query.categoryId() != null) {
            return MATCH_BY_CATEGORY;
        }
        if (query.q() != null) {
            return MATCH_BY_TEXT;
        }
        return "TRUE";
    }

    // Free text matches service or branch; structured filters match services only.
    private static String matchFilter(WorkshopSearchQuery query) {
        if (!hasTextFilter(query)) {
            return "";
        }
        List<String> alternatives = new ArrayList<>();
        alternatives.add(SERVICE_MATCH_PREDICATE.formatted(matchFragment(query)));
        if (query.q() != null) {
            alternatives.add(BRANCH_NAME_PREDICATE);
        }
        if (query.pinnedBranchId() != null) {
            alternatives.add(PINNED_PREDICATE);
        }
        return "  AND (" + String.join(" OR ", alternatives) + ")\n";
    }

    private static String filter(WorkshopSearchQuery query, Integer brandId) {
        /* Radius means "within X km of the point": the bbox replaces city/voivodeship,
           country stays so a radius near a border keeps to the searched country. */
        String locationFilter = query.radiusKm() != null
                ? (query.country() != null ? COUNTRY_FILTER : "") + BBOX_FILTER
                : locationFilter(query.city(), query.voivodeship(), query.country());
        return BRANCH_FILTER_BASE.formatted(locationFilter)
                + matchFilter(query)
                + (brandId != null ? BRAND_FILTER : "")
                + (query.availability() != null ? AVAILABILITY_FILTER : "");
    }

    private static String orderBy(boolean pinned, boolean sortByDistance) {
        return "ORDER BY "
                + (pinned ? PINNED_ORDER_EXPR + ", " : "")
                + (sortByDistance ? DISTANCE_ORDER_EXPR : NAME_ORDER_EXPR);
    }

    private static void bindFilterParams(Query nativeQuery, WorkshopSearchQuery query, Integer brandId) {
        if (query.radiusKm() != null) {
            if (query.country() != null) {
                nativeQuery.setParameter("country", query.country());
            }
            bindBoundingBox(nativeQuery, query.lat(), query.lng(), query.radiusKm());
        } else {
            bindLocationParams(nativeQuery, query.city(), query.voivodeship(), query.country());
        }
        bindMatchParams(nativeQuery, query);
        if (brandId != null) {
            nativeQuery.setParameter("brandId", brandId);
        }
        if (query.availability() != null) {
            bindAvailabilityParams(nativeQuery, query.availability());
        }
    }

    private static void bindOrderParams(
            Query nativeQuery, WorkshopSearchQuery query, boolean pinned, boolean sortByDistance) {
        if (pinned) {
            nativeQuery.setParameter("pinnedBranchId", query.pinnedBranchId());
        }
        if (sortByDistance) {
            nativeQuery.setParameter("lat", query.lat());
            nativeQuery.setParameter("lng", query.lng());
            nativeQuery.setParameter("lngScale", BigDecimal.valueOf(lngScale(query.lat())));
        }
    }

    private static void bindBoundingBox(Query nativeQuery, BigDecimal lat, BigDecimal lng, double radiusKm) {
        double deltaLat = radiusKm / KM_PER_DEGREE;
        double deltaLng = radiusKm / (KM_PER_DEGREE * lngScale(lat));
        nativeQuery.setParameter("latMin", lat.subtract(BigDecimal.valueOf(deltaLat)));
        nativeQuery.setParameter("latMax", lat.add(BigDecimal.valueOf(deltaLat)));
        nativeQuery.setParameter("lngMin", lng.subtract(BigDecimal.valueOf(deltaLng)));
        nativeQuery.setParameter("lngMax", lng.add(BigDecimal.valueOf(deltaLng)));
    }

    private static void bindMatchParams(Query nativeQuery, WorkshopSearchQuery query) {
        if (query.serviceName() != null) {
            nativeQuery.setParameter("serviceName", query.serviceName());
        } else if (query.categoryId() != null) {
            nativeQuery.setParameter("categoryId", query.categoryId());
        } else if (query.q() != null) {
            nativeQuery.setParameter("q", query.q());
            nativeQuery.setParameter("minSimilarity", MIN_WORD_SIMILARITY);
        }
    }

    private static void bindAvailabilityParams(Query nativeQuery, AvailabilityWindow window) {
        nativeQuery.setParameter("availFrom", window.from());
        nativeQuery.setParameter("availTo", window.to());
        nativeQuery.setParameter("winFromMin",
                window.timeFrom() != null ? window.timeFrom().toSecondOfDay() / 60 : 0);
        nativeQuery.setParameter("winToMin",
                window.timeTo() != null ? window.timeTo().toSecondOfDay() / 60 : MINUTES_PER_DAY);
    }

    private static double lngScale(BigDecimal lat) {
        return Math.cos(Math.toRadians(lat.doubleValue()));
    }

    private static Double distanceKm(BigDecimal lat, BigDecimal lng, BigDecimal branchLat, BigDecimal branchLng) {
        if (branchLat == null || branchLng == null) {
            return null;
        }
        double deltaLat = branchLat.doubleValue() - lat.doubleValue();
        double deltaLng = (branchLng.doubleValue() - lng.doubleValue()) * lngScale(lat);
        double km = KM_PER_DEGREE * Math.sqrt(deltaLat * deltaLat + deltaLng * deltaLng);
        return Math.round(km * 10.0) / 10.0;
    }
}
