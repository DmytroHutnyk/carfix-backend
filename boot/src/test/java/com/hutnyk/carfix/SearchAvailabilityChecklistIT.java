package com.hutnyk.carfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/** Real-HTTP M6 checklist derives expectations from dev DB and skips unsupported seed cases. */
@ActiveProfiles("dev")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "logging.level.root=WARN",
            "logging.level.org.hibernate.SQL=WARN",
            "logging.level.org.hibernate.orm.jdbc.bind=WARN"
        })
class SearchAvailabilityChecklistIT {

    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final String SEARCH_PATH = "/api/search/workshops";

    private static final String SERVICE_NAME = "Oil and filter change";
    private static final String CITY = "Warsaw";
    private static final String FREE_TEXT = "oil";

    private static final int GRID = 15;
    private static final int MAX_STARTS = 3;
    private static final int DEFAULT_SIZE = 20;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int MAX_PAGES_WALKED = 25;

    private static final LocalTime WINDOW_FROM = LocalTime.of(13, 0);
    private static final LocalTime WINDOW_TO = LocalTime.of(15, 0);
    private static final LocalTime AFTER_HOURS_FROM = LocalTime.of(22, 0);
    private static final LocalTime AFTER_HOURS_TO = LocalTime.of(23, 0);
    private static final LocalTime LAST_MINUTE = LocalTime.of(23, 59);

    private static final List<String> PROBLEM_FIELDS =
            List.of("type", "title", "status", "detail", "instance", "code");

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Chk> report = new ArrayList<>();


    private LocalDate today() {
        return LocalDate.now(WARSAW);
    }

    private LocalDate from() {
        return today().plusDays(1);
    }

    private LocalDate to() {
        return from().plusDays(2);
    }


    private static final class Chk {
        final String id;
        final String name;
        final List<String> fails = new ArrayList<>();
        final List<String> notes = new ArrayList<>();
        String skipped;

        Chk(String id, String name) {
            this.id = id;
            this.name = name;
        }

        void eq(String what, Object expected, Object actual) {
            if (!Objects.equals(String.valueOf(expected), String.valueOf(actual))) {
                fails.add(what + ": expected " + expected + ", got " + actual);
            }
        }

        void ok(String what, boolean cond) {
            if (!cond) {
                fails.add(what);
            }
        }

        void note(String n) {
            notes.add(n);
        }

        void skip(String reason) {
            skipped = reason;
        }

        boolean passed() {
            return fails.isEmpty();
        }

        String verdict() {
            if (!fails.isEmpty()) {
                return "**FAIL**";
            }
            return skipped != null ? "SKIP" : "PASS";
        }
    }

    private Chk check(String id, String name) {
        Chk c = new Chk(id, name);
        report.add(c);
        return c;
    }

    private ResponseEntity<String> raw(String path, String query) {
        try {
            return rest.getForEntity(new URI("http", null, "localhost", port, path, query, null), String.class);
        } catch (URISyntaxException e) {
            throw new AssertionError("unbuildable URI " + path + "?" + query, e);
        }
    }

    private JsonNode json(String path, String query) throws Exception {
        ResponseEntity<String> r = raw(path, query);
        if (r.getStatusCode().value() != 200) {
            throw new AssertionError(
                    "HTTP " + r.getStatusCode().value() + " for " + path + "?" + query + " -> " + r.getBody());
        }
        return mapper.readTree(r.getBody());
    }

    private JsonNode search(String query) throws Exception {
        return json(SEARCH_PATH, query);
    }

    private static String query(String... keyValues) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyValues.length; i += 2) {
            if (keyValues[i + 1] == null) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(keyValues[i]).append('=').append(keyValues[i + 1]);
        }
        return sb.toString();
    }

    private String windowQuery(LocalDate from, LocalDate to, LocalTime timeFrom, LocalTime timeTo) {
        return query(
                "serviceName", SERVICE_NAME,
                "city", CITY,
                "from", from == null ? null : from.toString(),
                "to", to == null ? null : to.toString(),
                "timeFrom", timeFrom == null ? null : timeFrom.toString(),
                "timeTo", timeTo == null ? null : timeTo.toString());
    }

    private static String slotsQuery(int serviceId, LocalDate from, LocalDate to) {
        return query("serviceIds", String.valueOf(serviceId), "from", from.toString(), "to", to.toString());
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static boolean absentOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull();
    }

    private static List<String> starts(JsonNode card) {
        List<String> out = new ArrayList<>();
        JsonNode starts = card.get("nextAvailableStarts");
        if (starts != null && !starts.isNull()) {
            starts.forEach(s -> out.add(s.get("date").asText() + " " + s.get("startTime").asText()));
        }
        return out;
    }

    private static int minuteOfDay(LocalTime time) {
        return time.toSecondOfDay() / 60;
    }


    // Mirrors SQL pre-filter; Java availability filtering may only remove these candidates.
    private long layer1Candidates(LocalDate from, LocalDate to, int winFromMin, int winToMin) {
        String sql = """
                SELECT count(*) FROM %1$s.branches b
                JOIN %1$s.addresses a ON a.address_id = b.address_id
                JOIN %1$s.cities c ON c.city_id = a.city_id
                WHERE b.status = 'ACTIVE' AND c.name ILIKE ?
                  AND EXISTS (
                      SELECT 1 FROM %1$s.services s2
                      JOIN %1$s.services_service_bay_types sbt ON sbt.service_id = s2.service_id
                      JOIN %1$s.service_bays sb ON sb.branch_id = b.branch_id
                                              AND sb.service_bay_type_id = sbt.service_bay_type_id
                                              AND sb.status = 'ACTIVE'
                      JOIN %1$s.service_bays_availability sba ON sba.service_bay_id = sb.service_bay_id
                      WHERE s2.branch_id = b.branch_id AND s2.status = 'ACTIVE' AND s2.name ILIKE ?
                        AND sba.date BETWEEN ? AND ?
                        AND sba.available_time && tsrange(
                                sba.date + (CAST(? AS int) * INTERVAL '1 minute'),
                                sba.date + (CAST(? AS int) * INTERVAL '1 minute'), '[)')
                  )
                """.formatted(schema);
        Long count = jdbc.queryForObject(sql, Long.class,
                CITY, SERVICE_NAME, Date.valueOf(from), Date.valueOf(to), winFromMin, winToMin);
        return count == null ? 0 : count;
    }

    private long offeringBranches() {
        String sql = """
                SELECT count(*) FROM %1$s.branches b
                JOIN %1$s.addresses a ON a.address_id = b.address_id
                JOIN %1$s.cities c ON c.city_id = a.city_id
                WHERE b.status = 'ACTIVE' AND c.name ILIKE ?
                  AND EXISTS (SELECT 1 FROM %1$s.services s WHERE s.branch_id = b.branch_id
                              AND s.status = 'ACTIVE' AND s.name ILIKE ?)
                """.formatted(schema);
        Long count = jdbc.queryForObject(sql, Long.class, CITY, SERVICE_NAME);
        return count == null ? 0 : count;
    }

    private long activeServicesLike(String pattern) {
        String sql = """
                SELECT count(*) FROM %1$s.services s
                JOIN %1$s.branches b ON b.branch_id = s.branch_id
                WHERE s.status = 'ACTIVE' AND b.status = 'ACTIVE' AND s.name ILIKE ?
                """.formatted(schema);
        Long count = jdbc.queryForObject(sql, Long.class, pattern);
        return count == null ? 0 : count;
    }

    private LocalTime latestCloseTime() {
        Time time = jdbc.queryForObject(
                "SELECT max(oh.close_time) FROM " + schema + ".opening_hours oh"
                        + " JOIN " + schema + ".branches b ON b.branch_id = oh.branch_id"
                        + " WHERE b.status = 'ACTIVE'",
                Time.class);
        return time == null ? null : time.toLocalTime();
    }

    private long availabilityRowsOn(LocalDate date) {
        String sql = """
                SELECT count(*) FROM %1$s.service_bays_availability sba
                JOIN %1$s.service_bays sb ON sb.service_bay_id = sba.service_bay_id
                JOIN %1$s.branches b ON b.branch_id = sb.branch_id
                JOIN %1$s.addresses a ON a.address_id = b.address_id
                JOIN %1$s.cities c ON c.city_id = a.city_id
                WHERE b.status = 'ACTIVE' AND c.name ILIKE ? AND sba.date = ?
                """.formatted(schema);
        Long count = jdbc.queryForObject(sql, Long.class, CITY, Date.valueOf(date));
        return count == null ? 0 : count;
    }

    private Integer anyCategoryId() {
        return jdbc.queryForObject(
                "SELECT min(service_category_id) FROM " + schema + ".service_categories", Integer.class);
    }


    private void assertCard(Chk c, JsonNode card, LocalDate from, LocalDate to,
                            LocalTime timeFrom, LocalTime timeTo) {
        String branch = card.get("branchId").asText();
        String tz = text(card, "tz");
        c.ok(branch + ": tz missing", tz != null && !tz.isBlank());
        if (tz != null) {
            try {
                ZoneId.of(tz);
            } catch (RuntimeException e) {
                c.fails.add(branch + ": tz '" + tz + "' is not an IANA zone id");
            }
        }

        JsonNode starts = card.get("nextAvailableStarts");
        if (starts == null || starts.isNull()) {
            c.fails.add(branch + ": nextAvailableStarts missing on the availability path");
            return;
        }
        c.ok(branch + ": nextAvailableStarts empty — such a branch must not be returned", starts.size() > 0);
        c.ok(branch + ": " + starts.size() + " starts exceed the cap of " + MAX_STARTS, starts.size() <= MAX_STARTS);

        LocalDateTime previous = null;
        for (JsonNode start : starts) {
            String rawTime = start.get("startTime").asText();
            c.ok(branch + ": startTime '" + rawTime + "' is not HH:mm",
                    rawTime.length() == 5 && rawTime.charAt(2) == ':');
            LocalDate date = LocalDate.parse(start.get("date").asText());
            LocalTime time = LocalTime.parse(rawTime);
            c.ok(branch + ": start " + date + " " + time + " outside the requested [" + from + "," + to + "]",
                    !date.isBefore(from) && !date.isAfter(to));
            c.ok(branch + ": startTime " + time + " off the " + GRID + "-minute grid",
                    time.getMinute() % GRID == 0 && time.getSecond() == 0);
            if (timeFrom != null) {
                c.ok(branch + ": startTime " + time + " before the requested timeFrom " + timeFrom,
                        !time.isBefore(timeFrom));
            }
            if (timeTo != null) {
                c.ok(branch + ": startTime " + time + " not before the requested timeTo " + timeTo,
                        time.isBefore(timeTo));
            }
            LocalDateTime at = LocalDateTime.of(date, time);
            if (previous != null) {
                c.ok(branch + ": starts not ascending (" + previous + " then " + at + ")", at.isAfter(previous));
            }
            previous = at;
        }
    }

    private void assertEcho(Chk c, JsonNode body, LocalDate from, LocalDate to,
                            LocalTime timeFrom, LocalTime timeTo) {
        JsonNode echo = body.get("echo");
        if (echo == null || echo.isNull()) {
            c.fails.add("echo missing from the page");
            return;
        }
        if (from == null) {
            c.ok("echo.availability must be null without date params, got " + echo.get("availability"),
                    absentOrNull(echo, "availability"));
            return;
        }
        JsonNode availability = echo.get("availability");
        if (availability == null || availability.isNull()) {
            c.fails.add("echo.availability missing although from/to were sent");
            return;
        }
        c.eq("echo.availability.from", from.toString(), text(availability, "from"));
        c.eq("echo.availability.to", to.toString(), text(availability, "to"));
        c.eq("echo.availability.timeFrom", timeFrom == null ? null : timeFrom.toString(),
                text(availability, "timeFrom"));
        c.eq("echo.availability.timeTo", timeTo == null ? null : timeTo.toString(),
                text(availability, "timeTo"));
    }

    private Set<String> slotStarts(String branchId, int serviceId, LocalDate from, LocalDate to) throws Exception {
        JsonNode slots = json("/api/branches/" + branchId + "/slots", slotsQuery(serviceId, from, to));
        Set<String> out = new LinkedHashSet<>();
        for (JsonNode day : slots.get("days")) {
            String date = day.get("date").asText();
            day.get("slots").forEach(s -> out.add(date + " " + s.get("startTime").asText()));
        }
        return out;
    }


    @Test
    void runManualChecklist() throws Exception {
        check1();
        check2();
        check3();
        check4();
        check5();
        check6();
        check7();
        check8();
        check9();

        StringBuilder sb = new StringBuilder(
                "\n\n==================== SEARCH AVAILABILITY CHECKLIST RESULT ====================\n");
        sb.append("run at ").append(LocalDateTime.now(WARSAW).withNano(0)).append(" Warsaw; window ")
                .append(from()).append(" .. ").append(to())
                .append("; service '").append(SERVICE_NAME).append("'; city ").append(CITY).append("\n");
        sb.append("| # | check | pass / fail | notes |\n|---|---|---|---|\n");
        for (Chk c : report) {
            List<String> shown = new ArrayList<>();
            if (!c.passed()) {
                shown.addAll(c.fails);
            } else {
                if (c.skipped != null) {
                    shown.add("SKIPPED: " + c.skipped);
                }
                shown.addAll(c.notes);
            }
            sb.append("| ").append(c.id).append(" | ").append(c.name).append(" | ")
                    .append(c.verdict()).append(" | ").append(String.join("; ", shown)).append(" |\n");
        }
        long failed = report.stream().filter(c -> !c.passed()).count();
        sb.append("\n").append(report.size() - failed).append("/").append(report.size()).append(" checks passed\n");
        for (Chk c : report) {
            if (!c.passed()) {
                sb.append("\n--- FAIL DETAIL ").append(c.id).append(" (").append(c.name).append(") ---\n");
                c.fails.forEach(f -> sb.append("  * ").append(f).append("\n"));
                c.notes.forEach(n -> sb.append("  . ").append(n).append("\n"));
            }
        }
        sb.append("==============================================================================\n");
        System.out.println(sb);

        if (failed > 0) {
            throw new AssertionError(failed + " checklist check(s) failed — see table above");
        }
    }

    private void check1() {
        Chk c = check("1", "availability window (serviceName + city + from/to)");
        try {
            LocalDate from = from();
            LocalDate to = to();
            long candidates = layer1Candidates(from, to, 0, MINUTES_PER_DAY);
            c.note("SQL pre-filter matches " + candidates + " branch(es)");
            if (candidates == 0) {
                c.skip("no ACTIVE " + CITY + " branch offers '" + SERVICE_NAME + "' with bay availability in "
                        + from + ".." + to + " — reseed the DB (V53/V55)");
                return;
            }

            JsonNode body = search(windowQuery(from, to, null, null));
            JsonNode content = body.get("content");
            c.ok("content is empty although " + candidates + " branch(es) pass the SQL pre-filter"
                    + " — either layer 2 is broken or every candidate lacks staff/equipment",
                    content.size() > 0);
            c.ok("layer 2 returned " + content.size() + " cards but only " + candidates
                    + " branch(es) pass the SQL pre-filter", content.size() <= candidates);

            Set<String> branchIds = new LinkedHashSet<>();
            for (JsonNode card : content) {
                assertCard(c, card, from, to, null, null);
                branchIds.add(card.get("branchId").asText());
            }
            c.eq("distinct branchIds", content.size(), branchIds.size());

            assertEcho(c, body, from, to, null, null);
            c.eq("echo.serviceName", SERVICE_NAME, text(body.get("echo"), "serviceName"));
            c.eq("echo.city", CITY, text(body.get("echo"), "city"));
            c.eq("page", 0, body.get("page").asInt());
            c.eq("size", DEFAULT_SIZE, body.get("size").asInt());

            long total = body.get("totalElements").asLong();
            if (total <= DEFAULT_SIZE) {
                c.eq("single page: totalElements == content.size()", total, (long) content.size());
                c.eq("totalPages", total == 0 ? 0 : 1, body.get("totalPages").asInt());
            } else {
                c.note("totalElements " + total + " exceeds the default page size — paging is check 6");
            }
            c.note(content.size() + " card(s), each with tz and <= " + MAX_STARTS
                    + " ascending grid-aligned starts inside " + from + ".." + to);
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check2() {
        Chk c = check("2", "after-hours window returns nothing");
        try {
            LocalTime latestClose = latestCloseTime();
            if (latestClose == null) {
                c.skip("no opening hours seeded — cannot derive an after-hours window");
                return;
            }
            LocalTime timeFrom = latestClose.isAfter(AFTER_HOURS_FROM) ? latestClose : AFTER_HOURS_FROM;
            LocalTime timeTo = timeFrom.isBefore(AFTER_HOURS_TO) ? AFTER_HOURS_TO : LAST_MINUTE;
            if (!timeFrom.isBefore(timeTo)) {
                c.skip("branches close at " + latestClose + " — no after-hours window left before midnight");
                return;
            }
            c.note("latest close_time " + latestClose + " -> window " + timeFrom + "-" + timeTo);

            LocalDate from = from();
            LocalDate to = to();
            c.eq("SQL pre-filter for the closed window", 0L,
                    layer1Candidates(from, to, minuteOfDay(timeFrom), minuteOfDay(timeTo)));

            JsonNode body = search(windowQuery(from, to, timeFrom, timeTo));
            c.eq("content", 0, body.get("content").size());
            c.eq("totalElements", 0L, body.get("totalElements").asLong());
            c.eq("totalPages", 0, body.get("totalPages").asInt());
            assertEcho(c, body, from, to, timeFrom, timeTo);
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check3() {
        Chk c = check("3", "starts agree with /api/branches/{id}/slots");
        try {
            LocalDate from = from();
            LocalDate to = to();
            JsonNode content = search(windowQuery(from, to, null, null)).get("content");
            if (content.size() == 0) {
                c.skip("the check-1 query returned no card to compare against /slots");
                return;
            }
            JsonNode card = content.get(0);
            JsonNode matched = card.get("matchedServices");
            if (matched == null || matched.size() != 1) {
                c.skip("first card matches " + (matched == null ? 0 : matched.size())
                        + " services — its starts are the union over all of them, which no single-service"
                        + " /slots call reproduces");
                return;
            }

            String branchId = card.get("branchId").asText();
            int serviceId = matched.get(0).get("serviceId").asInt();
            JsonNode slots = json("/api/branches/" + branchId + "/slots", slotsQuery(serviceId, from, to));
            c.eq("tz", card.get("tz").asText(), slots.get("tz").asText());
            c.eq("chainable", true, slots.get("chainable").asBoolean());

            List<String> fromSlots = new ArrayList<>();
            for (JsonNode day : slots.get("days")) {
                String date = day.get("date").asText();
                for (JsonNode slot : day.get("slots")) {
                    if (fromSlots.size() >= MAX_STARTS) {
                        break;
                    }
                    fromSlots.add(date + " " + slot.get("startTime").asText());
                }
                if (fromSlots.size() >= MAX_STARTS) {
                    break;
                }
            }
            c.eq("first " + MAX_STARTS + " (date, startTime) of branch " + branchId + " service " + serviceId,
                    fromSlots, starts(card));
            c.note("branch " + branchId + " service " + serviceId + ": " + fromSlots);
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check4() {
        Chk c = check("4", "time-of-day window " + WINDOW_FROM + "-" + WINDOW_TO);
        try {
            LocalDate from = from();
            LocalDate to = to();
            long candidates = layer1Candidates(from, to, minuteOfDay(WINDOW_FROM), minuteOfDay(WINDOW_TO));
            c.note("SQL pre-filter matches " + candidates + " branch(es)");
            if (candidates == 0) {
                c.skip("no ACTIVE " + CITY + " branch is open between " + WINDOW_FROM + " and " + WINDOW_TO
                        + " on " + from + ".." + to + " — reseed the DB (V53/V55)");
                return;
            }

            JsonNode body = search(windowQuery(from, to, WINDOW_FROM, WINDOW_TO));
            JsonNode content = body.get("content");
            c.ok("content is empty although " + candidates + " branch(es) pass the SQL pre-filter"
                    + " — either the time window is dropped or every candidate lacks staff/equipment",
                    content.size() > 0);
            c.ok("layer 2 returned " + content.size() + " cards but only " + candidates
                    + " branch(es) pass the SQL pre-filter", content.size() <= candidates);
            assertEcho(c, body, from, to, WINDOW_FROM, WINDOW_TO);

            int compared = 0;
            for (JsonNode card : content) {
                assertCard(c, card, from, to, WINDOW_FROM, WINDOW_TO);
                JsonNode matched = card.get("matchedServices");
                if (matched == null || matched.size() != 1) {
                    continue;
                }
                String branchId = card.get("branchId").asText();
                int serviceId = matched.get(0).get("serviceId").asInt();
                Set<String> all = slotStarts(branchId, serviceId, from, to);
                for (String start : starts(card)) {
                    c.ok(branchId + ": start " + start + " is not among the /slots starts for service "
                            + serviceId, all.contains(start));
                }
                compared++;
            }
            c.ok("no card had a single matched service to cross-check against /slots", compared > 0);
            c.note(compared + " of " + content.size() + " card(s) cross-checked against /slots");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check5() {
        Chk c = check("5", "validation (400 INVALID_SEARCH_FILTER)");
        LocalDate from = from();
        LocalDate to = to();
        Integer categoryId = null;
        try {
            categoryId = anyCategoryId();
        } catch (RuntimeException e) {
            c.note("no service category to build case b from: " + e);
        }

        Map<String, String[]> cases = new LinkedHashMap<>();
        cases.put("a free text + dates", new String[] {
            query("q", FREE_TEXT, "from", from.toString(), "to", to.toString()), "INVALID_SEARCH_FILTER"});
        if (categoryId != null) {
            cases.put("b categoryId + dates", new String[] {
                query("categoryId", categoryId.toString(), "from", from.toString(), "to", to.toString()),
                "INVALID_SEARCH_FILTER"});
        } else {
            c.note("case b skipped — no service category seeded");
        }
        cases.put("c from without to", new String[] {
            windowQuery(from, null, null, null), "INVALID_SEARCH_FILTER"});
        cases.put("d 8-day range", new String[] {
            windowQuery(from, from.plusDays(7), null, null), "INVALID_SEARCH_FILTER"});
        cases.put("e timeTo before timeFrom", new String[] {
            windowQuery(from, to, WINDOW_TO, WINDOW_FROM), "INVALID_SEARCH_FILTER"});
        cases.put("f unparsable timeFrom", new String[] {
            query("serviceName", SERVICE_NAME, "from", from.toString(), "to", to.toString(), "timeFrom", "1pm"),
            null});

        int exact = 0;
        for (Map.Entry<String, String[]> e : cases.entrySet()) {
            String label = e.getKey();
            String queryString = e.getValue()[0];
            String expectedCode = e.getValue()[1];
            int before = c.fails.size();
            try {
                ResponseEntity<String> r = raw(SEARCH_PATH, queryString);
                c.eq(label + " status", 400, r.getStatusCode().value());
                JsonNode body = mapper.readTree(r.getBody());
                for (String field : PROBLEM_FIELDS) {
                    c.ok(label + " body missing '" + field + "' (got " + body + ")", body.has(field));
                }
                if (expectedCode != null && body.has("code")) {
                    c.eq(label + " code", expectedCode, body.get("code").asText());
                }
                if (expectedCode == null && body.has("code")) {
                    c.note(label + " code " + body.get("code").asText());
                }
                if (body.has("instance")) {
                    c.eq(label + " instance", SEARCH_PATH, body.get("instance").asText());
                }
                if (body.has("detail")) {
                    String detail = body.get("detail").asText();
                    c.ok(label + " detail leaks internals: " + detail,
                            !detail.contains("\tat ")
                                    && !detail.contains("com.hutnyk")
                                    && !detail.toLowerCase().contains("select ")
                                    && !detail.contains("org.springframework")
                                    && !detail.contains("Exception:"));
                    c.note(label + ": " + detail);
                }
            } catch (Throwable t) {
                c.fails.add(label + " threw: " + t);
            }
            if (c.fails.size() == before) {
                exact++;
            }
        }
        c.note(exact + "/" + cases.size() + " cases exact (status + code + problem fields + instance)");
    }

    private void check6() {
        Chk c = check("6", "paging after the availability filter");
        try {
            LocalDate from = from();
            LocalDate to = to();
            String base = windowQuery(from, to, null, null);
            JsonNode first = search(base + "&size=2&page=0");
            long total = first.get("totalElements").asLong();
            if (total == 0) {
                c.skip("the check-1 query returned nothing to page");
                return;
            }
            int totalPages = first.get("totalPages").asInt();
            c.eq("size echo", 2, first.get("size").asInt());
            c.eq("totalPages", (int) Math.ceil(total / 2.0), totalPages);

            int walked = Math.min(totalPages, MAX_PAGES_WALKED);
            if (walked < totalPages) {
                c.note("walked the first " + walked + " of " + totalPages + " pages only");
            }
            Set<String> branchIds = new LinkedHashSet<>();
            for (int page = 0; page < walked; page++) {
                JsonNode body = page == 0 ? first : search(base + "&size=2&page=" + page);
                c.eq("page " + page + " echo", page, body.get("page").asInt());
                c.eq("page " + page + " totalElements", total, body.get("totalElements").asLong());
                c.eq("page " + page + " totalPages", totalPages, body.get("totalPages").asInt());
                JsonNode content = body.get("content");
                c.ok("page " + page + " holds " + content.size() + " cards, more than the requested 2",
                        content.size() <= 2);
                for (JsonNode card : content) {
                    String branchId = card.get("branchId").asText();
                    c.ok("branch " + branchId + " appears on more than one page", branchIds.add(branchId));
                    assertCard(c, card, from, to, null, null);
                }
            }
            if (walked == totalPages) {
                c.eq("distinct branchIds across all pages", total, (long) branchIds.size());
            }
            c.note(total + " branch(es) over " + totalPages + " page(s) of 2");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check7() {
        Chk c = check("7", "closed day (next Sunday) is empty");
        try {
            LocalDate sunday = today().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            long rows = availabilityRowsOn(sunday);
            if (rows > 0) {
                c.skip("the seed now holds " + rows + " bay availability row(s) on " + sunday
                        + " — Sundays are no longer closed, pick another closed day");
                return;
            }
            JsonNode body = search(windowQuery(sunday, sunday, null, null));
            c.eq("content", 0, body.get("content").size());
            c.eq("totalElements", 0L, body.get("totalElements").asLong());
            c.eq("totalPages", 0, body.get("totalPages").asInt());
            assertEcho(c, body, sunday, sunday, null, null);
            c.note(sunday + " (SUNDAY) has no bay availability rows; the endpoint returns an empty page");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check8() {
        Chk c = check("8", "plain search carries tz but no availability payload");
        try {
            long offering = offeringBranches();
            c.note(offering + " ACTIVE " + CITY + " branch(es) offer '" + SERVICE_NAME + "'");
            if (offering == 0) {
                c.skip("no ACTIVE " + CITY + " branch offers '" + SERVICE_NAME + "' — reseed the DB (V53)");
                return;
            }

            JsonNode body = search(query("serviceName", SERVICE_NAME, "city", CITY));
            JsonNode content = body.get("content");
            c.ok("content is empty although " + offering + " branch(es) offer the service", content.size() > 0);
            if (offering <= DEFAULT_SIZE) {
                c.eq("totalElements", offering, body.get("totalElements").asLong());
            }
            for (JsonNode card : content) {
                String branchId = card.get("branchId").asText();
                String tz = text(card, "tz");
                c.ok(branchId + ": tz missing on the plain path", tz != null && !tz.isBlank());
                c.ok(branchId + ": nextAvailableStarts must be absent or null without date params, got "
                        + card.get("nextAvailableStarts"), absentOrNull(card, "nextAvailableStarts"));
            }
            assertEcho(c, body, null, null, null, null);
            c.eq("echo.serviceName", SERVICE_NAME, text(body.get("echo"), "serviceName"));
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private void check9() {
        Chk c = check("9", "free-text search regression guard");
        try {
            long matches = activeServicesLike("%" + FREE_TEXT + "%");
            c.note(matches + " ACTIVE service(s) of ACTIVE branches match '%" + FREE_TEXT + "%'");
            if (matches == 0) {
                c.skip("no ACTIVE service name contains '" + FREE_TEXT + "' — reseed the DB (V53)");
                return;
            }

            JsonNode body = search(query("q", FREE_TEXT));
            JsonNode content = body.get("content");
            c.ok("content is empty although " + matches + " service(s) match '" + FREE_TEXT + "'",
                    content.size() > 0);
            c.ok("totalElements must be positive", body.get("totalElements").asLong() > 0);

            int withoutMatchedServices = 0;
            for (JsonNode card : content) {
                String branchId = card.get("branchId").asText();
                c.ok(branchId + ": tz missing", text(card, "tz") != null);
                c.ok(branchId + ": nextAvailableStarts must be absent or null without date params, got "
                        + card.get("nextAvailableStarts"), absentOrNull(card, "nextAvailableStarts"));
                if (card.get("matchedServices").size() == 0) {
                    withoutMatchedServices++;
                }
            }
            if (withoutMatchedServices > 0) {
                c.note(withoutMatchedServices + " card(s) matched on the branch name only (no matchedServices)");
            }
            assertEcho(c, body, null, null, null, null);
            c.eq("echo.q", FREE_TEXT, text(body.get("echo"), "q"));
            c.note(content.size() + " card(s) on page 0 of " + body.get("totalElements").asLong());
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }
}
