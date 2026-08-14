package com.hutnyk.carfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.test.context.ActiveProfiles;

/**
 * Run-time robust: every query window is derived from the clock at run time and every expected
 * number is derived from the seeded opening hours and service durations, so the verdict does not
 * depend on which calendar day, weekday or hour the harness runs.
 *
 * <p>Checks 1-6 deliberately query a window that starts TOMORROW: today is legitimately clamped to
 * "now rounded up to the 15-minute grid", which makes full-day expectations meaningless. The clamp
 * itself is covered by check 7, which is the only today-based check.
 */
@ActiveProfiles("dev")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "logging.level.root=WARN",
            "logging.level.org.hibernate.SQL=WARN",
            "logging.level.org.hibernate.orm.jdbc.bind=WARN"
        })
class SlotEndpointChecklistIT {

    private static final String BRANCH_A = "10000000-0000-4000-8000-000000000001";
    private static final String BRANCH_B = "10000000-0000-4000-8000-000000000002";
    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
    private static final int GRID = 15;

    private static final int SVC_OIL = 11; // 60 min, branch A
    private static final int SVC_BRAKES = 27; // 90 min, branch A
    private static final int SVC_ENGINE = 1; // 480 min, branch A
    private static final int SVC_CLUTCH = 4; // 300 min, branch A
    private static final int SVC_TYRE_B = 35; // 40 min, branch B

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Chk> report = new ArrayList<>();

    // -------------------------------------------------------------- run window

    private LocalDate today() {
        return LocalDate.now(WARSAW);
    }

    private LocalDate from() {
        return today().plusDays(1);
    }

    private LocalDate to() {
        return from().plusDays(6);
    }

    private List<LocalDate> window() {
        List<LocalDate> out = new ArrayList<>();
        for (LocalDate d = from(); !d.isAfter(to()); d = d.plusDays(1)) {
            out.add(d);
        }
        return out;
    }

    private static List<String> asText(List<LocalDate> dates) {
        List<String> out = new ArrayList<>();
        dates.forEach(d -> out.add(d.toString()));
        return out;
    }

    // ---------------------------------------------------------------- harness

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

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private ResponseEntity<String> raw(String path) {
        return rest.getForEntity(url(path), String.class);
    }

    private JsonNode json(String path) throws Exception {
        ResponseEntity<String> r = raw(path);
        if (r.getStatusCode().value() != 200) {
            throw new AssertionError("HTTP " + r.getStatusCode().value() + " for " + path + " -> " + r.getBody());
        }
        return mapper.readTree(r.getBody());
    }

    private String slotsUrl(String branchId, String serviceIds, LocalDate from, LocalDate to) {
        return "/api/branches/" + branchId + "/slots?serviceIds=" + serviceIds + "&from=" + from + "&to=" + to;
    }

    private static JsonNode day(JsonNode body, String date) {
        for (JsonNode d : body.get("days")) {
            if (date.equals(d.get("date").asText())) {
                return d;
            }
        }
        throw new AssertionError("day " + date + " missing from response");
    }

    private static List<String> dates(JsonNode body) {
        List<String> out = new ArrayList<>();
        body.get("days").forEach(d -> out.add(d.get("date").asText()));
        return out;
    }

    private static int count(JsonNode day) {
        return day.get("slots").size();
    }

    private static String span(JsonNode day, int i) {
        JsonNode s = day.get("slots").get(i);
        return s.get("startTime").asText() + "-" + s.get("endTime").asText();
    }

    private static String first(JsonNode day) {
        return count(day) == 0 ? "(none)" : span(day, 0);
    }

    private static String last(JsonNode day) {
        return count(day) == 0 ? "(none)" : span(day, count(day) - 1);
    }

    /** Verifies grid alignment, exact duration, and 15-minute ascending step across a day. */
    private static void shape(Chk c, JsonNode day, String date, int expectedMinutes) {
        LocalTime prev = null;
        for (JsonNode s : day.get("slots")) {
            LocalTime st = LocalTime.parse(s.get("startTime").asText());
            LocalTime en = LocalTime.parse(s.get("endTime").asText());
            if (st.getMinute() % GRID != 0 || st.getSecond() != 0) {
                c.fails.add(date + ": startTime " + st + " off the 15-minute grid");
            }
            long mins = Duration.between(st, en).toMinutes();
            if (mins != expectedMinutes) {
                c.fails.add(date + ": slot " + st + "-" + en + " spans " + mins + " min, expected " + expectedMinutes);
            }
            // Ascending and grid-aligned. NOT a constant 15-min step: a booked window
            // legitimately opens a wider gap between consecutive slots (see check 6).
            if (prev != null) {
                long step = Duration.between(prev, st).toMinutes();
                if (step <= 0 || step % GRID != 0) {
                    c.fails.add(date + ": step " + prev + " -> " + st + " is not a positive multiple of 15 min");
                }
            }
            prev = st;
        }
    }

    // --------------------------------------------------------------- fixtures

    /** Opening hours of a branch, keyed by weekday. A missing key means the branch is closed. */
    private record Hours(LocalTime open, LocalTime close) {}

    private Map<DayOfWeek, Hours> hours(String branchId) {
        Map<DayOfWeek, Hours> out = new EnumMap<>(DayOfWeek.class);
        jdbc.query(
                "SELECT day_of_week, start_time, close_time FROM " + schema
                        + ".opening_hours WHERE branch_id = CAST(? AS uuid)",
                (RowCallbackHandler)
                        rs ->
                                out.put(
                                        DayOfWeek.valueOf(rs.getString("day_of_week")),
                                        new Hours(
                                                rs.getTime("start_time").toLocalTime(),
                                                rs.getTime("close_time").toLocalTime())),
                branchId);
        return out;
    }

    /**
     * Dates inside [from,to] on which the branch holds ANY resource occupancy. Full-day
     * expectations are derived from the opening hours alone, so they only hold on days with no
     * bookings; V56 anchors its seeded bookings to CURRENT_DATE at migration time, so which days
     * those are moves with every reseed.
     */
    private Set<LocalDate> busyDates(String branchId, LocalDate from, LocalDate to) {
        String sql =
                "SELECT DISTINCT d FROM ("
                        + "  SELECT sbb.date AS d FROM " + schema + ".service_bays_bookings sbb"
                        + "    JOIN " + schema + ".service_bays sb ON sb.service_bay_id = sbb.service_bay_id"
                        + "   WHERE sb.branch_id = CAST(? AS uuid)"
                        + "  UNION ALL"
                        + "  SELECT eb.date FROM " + schema + ".employees_bookings eb"
                        + "    JOIN " + schema + ".employees e ON e.user_id = eb.employee_id"
                        + "   WHERE e.branch_id = CAST(? AS uuid)"
                        + "  UNION ALL"
                        + "  SELECT qb.date FROM " + schema + ".equipment_bookings qb"
                        + "    JOIN " + schema + ".equipment q ON q.equipment_id = qb.equipment_id"
                        + "   WHERE q.branch_id = CAST(? AS uuid)"
                        + ") x WHERE d BETWEEN ? AND ?";
        Set<LocalDate> out = new HashSet<>();
        jdbc.query(
                sql,
                (RowCallbackHandler) rs -> out.add(rs.getDate("d").toLocalDate()),
                branchId,
                branchId,
                branchId,
                java.sql.Date.valueOf(from),
                java.sql.Date.valueOf(to));
        return out;
    }

    /** Last date for which the seed generated availability rows for a branch. */
    private LocalDate availabilityHorizon(String branchId) {
        java.sql.Date d =
                jdbc.queryForObject(
                        "SELECT max(sba.date) FROM " + schema + ".service_bays_availability sba"
                                + " JOIN " + schema + ".service_bays sb ON sb.service_bay_id = sba.service_bay_id"
                                + " WHERE sb.branch_id = CAST(? AS uuid)",
                        java.sql.Date.class,
                        branchId);
        return d == null ? null : d.toLocalDate();
    }

    // ------------------------------------------------------- slot arithmetic

    /** Number of grid-aligned starts of {@code duration} that fit in [open,close). */
    private static int fits(LocalTime open, LocalTime close, int duration) {
        long window = Duration.between(open, close).toMinutes();
        return window < duration ? 0 : (int) ((window - duration) / GRID) + 1;
    }

    private static LocalTime lastStart(LocalTime open, LocalTime close, int duration) {
        return open.plusMinutes((long) (fits(open, close, duration) - 1) * GRID);
    }

    private static String spanOf(LocalTime start, int duration) {
        return start + "-" + start.plusMinutes(duration);
    }

    /**
     * Asserts the full-day shape of every day in the window, deriving each day's expectation from
     * the branch's opening hours for that weekday. Days on which the branch holds bookings are
     * reported and skipped: their reduced counts are correct behaviour, not a deviation.
     */
    private void assertWindow(
            Chk c,
            JsonNode body,
            String branchId,
            Map<DayOfWeek, Hours> hours,
            Set<LocalDate> busy,
            int duration,
            String nonEmptyMessage) {
        List<LocalDate> window = window();
        c.eq("day count", window.size(), body.get("days").size());
        c.eq("dates in order", asText(window), dates(body));

        int asserted = 0;
        for (LocalDate d : window) {
            JsonNode day = day(body, d.toString());
            Hours h = hours.get(d.getDayOfWeek());
            if (h == null) {
                c.eq(d + " (" + d.getDayOfWeek() + ", closed) empty", 0, count(day));
                continue;
            }
            if (busy.contains(d)) {
                c.note(d + " skipped — branch holds bookings that day (" + count(day) + " slots)");
                continue;
            }
            int expected = fits(h.open(), h.close(), duration);
            if (expected == 0) {
                c.eq(d + " (" + h.open() + "-" + h.close() + ") too short for " + duration + " min", 0, count(day));
                asserted++;
                continue;
            }
            if (nonEmptyMessage != null) {
                c.ok(d + " must be NON-EMPTY — " + nonEmptyMessage, count(day) > 0);
            }
            c.eq(d + " slot count", expected, count(day));
            c.eq(d + " first", spanOf(h.open(), duration), first(day));
            c.eq(d + " last", spanOf(lastStart(h.open(), h.close(), duration), duration), last(day));
            shape(c, day, d.toString(), duration);
            asserted++;
        }
        c.ok("no open day left to assert — reseed the DB or widen the window", asserted > 0);

        LocalDate horizon = availabilityHorizon(branchId);
        if (horizon != null && horizon.isBefore(to())) {
            c.note("availability horizon " + horizon + " ends inside the window — reseed (V55)");
        }
    }

    // ------------------------------------------------------------------ tests

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

        StringBuilder sb = new StringBuilder("\n\n==================== SLOT CHECKLIST RESULT ====================\n");
        sb.append("run at ").append(LocalDateTime.now(WARSAW).withNano(0)).append(" Warsaw; checks 1-6 window ")
                .append(from()).append(" .. ").append(to()).append("\n");
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
        sb.append("===============================================================\n");
        System.out.println(sb);

        if (failed > 0) {
            throw new AssertionError(failed + " checklist check(s) failed — see table above");
        }
    }

    /** 1. Single simple service, one week. */
    private void check1() throws Exception {
        Chk c = check("1", "single service, one week");
        try {
            JsonNode b = json(slotsUrl(BRANCH_A, String.valueOf(SVC_OIL), from(), to()));
            c.eq("chainable", true, b.get("chainable").asBoolean());
            assertWindow(c, b, BRANCH_A, hours(BRANCH_A), busyDates(BRANCH_A, from(), to()), 60, null);
            c.note("7 days, every open day full 60-min grid, closed days present and empty");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 2. Engine replacement — the greedy-matching trap. */
    private void check2() throws Exception {
        Chk c = check("2", "engine replacement (greedy trap)");
        try {
            JsonNode b = json(slotsUrl(BRANCH_A, String.valueOf(SVC_ENGINE), from(), to()));
            assertWindow(
                    c,
                    b,
                    BRANCH_A,
                    hours(BRANCH_A),
                    busyDates(BRANCH_A, from(), to()),
                    480,
                    "Kuhn augmenting path regressed (the sole senior mechanic must be reassigned)");
            c.note("full days non-empty at 480 min; short days (Sat 09:00-14:00) correctly empty");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 3. Clutch replacement — two trolley jacks drawn from one pool of two. */
    private void check3() throws Exception {
        Chk c = check("3", "clutch replacement (two jacks)");
        try {
            JsonNode b = json(slotsUrl(BRANCH_A, String.valueOf(SVC_CLUTCH), from(), to()));
            assertWindow(
                    c,
                    b,
                    BRANCH_A,
                    hours(BRANCH_A),
                    busyDates(BRANCH_A, from(), to()),
                    300,
                    "both jack requirements must draw distinct units from the pool of two");
            c.note("300-min visits; Sat 09:00-14:00 gives exactly the one exact-fit slot");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 4. Disjoint bay types -> chainable:false, 200, all days empty. */
    private void check4() throws Exception {
        Chk c = check("4", "disjoint bay types -> chainable false");
        try {
            ResponseEntity<String> r = raw(slotsUrl(BRANCH_A, "43,4", from(), to()));
            c.eq("status", 200, r.getStatusCode().value());
            JsonNode b = mapper.readTree(r.getBody());
            c.eq("chainable", false, b.get("chainable").asBoolean());
            c.eq("day count", window().size(), b.get("days").size());
            c.eq("dates", asText(window()), dates(b));
            for (LocalDate d : window()) {
                c.eq(d + " empty", 0, count(day(b, d.toString())));
            }

            JsonNode alt = json(slotsUrl(BRANCH_A, "33,1", from(), to()));
            c.eq("second pair 33,1 chainable", false, alt.get("chainable").asBoolean());
            c.note("200 + chainable:false, all 7 days present and empty; 33,1 agrees");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 5. Chainable pair -> one contiguous 150-minute visit. */
    private void check5() throws Exception {
        Chk c = check("5", "chainable pair");
        try {
            JsonNode b = json(slotsUrl(BRANCH_A, SVC_OIL + "," + SVC_BRAKES, from(), to()));
            c.eq("chainable", true, b.get("chainable").asBoolean());
            assertWindow(c, b, BRANCH_A, hours(BRANCH_A), busyDates(BRANCH_A, from(), to()), 150, null);
            c.note("chainable:true, every slot exactly 150 min (60+90, no gap leak)");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /**
     * 6. A booked window must be excluded from the day it falls on.
     *
     * <p>The booking is picked from the DB rather than pinned to a date: V56 anchors its bookings to
     * CURRENT_DATE at migration time, so a reseed always drops a fresh branch-B bay booking within a
     * week or so of the reseed, and this check keeps working across reseeds instead of dying for
     * good the day the pinned date passes. If the DB has drifted far enough that no such booking is
     * left inside the queried window the check reports SKIPPED with that reason.
     */
    private void check6() throws Exception {
        Chk c = check("6", "booked window excluded");
        try {
            String bayFilter =
                    "sb.branch_id = CAST(? AS uuid) AND sb.status = 'ACTIVE' AND sb.service_bay_type_id IN"
                            + " (SELECT sbt.service_bay_type_id FROM " + schema
                            + ".services_service_bay_types sbt WHERE sbt.service_id = " + SVC_TYRE_B + ")";

            Integer bays =
                    jdbc.queryForObject(
                            "SELECT count(*) FROM " + schema + ".service_bays sb WHERE " + bayFilter,
                            Integer.class,
                            BRANCH_B);
            if (bays == null || bays != 1) {
                c.skip("branch B now has " + bays + " active bays for service " + SVC_TYRE_B
                        + " — a single bay booking no longer blocks the whole window");
                return;
            }

            List<Map<String, Object>> rows =
                    jdbc.queryForList(
                            "SELECT sbb.date AS d, lower(sbb.booked_time)::time AS s,"
                                    + " upper(sbb.booked_time)::time AS e FROM " + schema
                                    + ".service_bays_bookings sbb JOIN " + schema
                                    + ".service_bays sb ON sb.service_bay_id = sbb.service_bay_id"
                                    + " WHERE " + bayFilter + " AND sbb.date BETWEEN ? AND ? ORDER BY sbb.date",
                            BRANCH_B,
                            java.sql.Date.valueOf(from()),
                            java.sql.Date.valueOf(to()));

            Map<DayOfWeek, Hours> hours = hours(BRANCH_B);
            LocalDate date = null;
            LocalTime bookedStart = null;
            LocalTime bookedEnd = null;
            for (Map<String, Object> row : rows) {
                LocalDate d = ((java.sql.Date) row.get("d")).toLocalDate();
                long sameDay = rows.stream().filter(r -> ((java.sql.Date) r.get("d")).toLocalDate().equals(d)).count();
                Hours h = hours.get(d.getDayOfWeek());
                LocalTime s = ((Time) row.get("s")).toLocalTime();
                LocalTime e = ((Time) row.get("e")).toLocalTime();
                if (sameDay == 1 && h != null && !s.isBefore(h.open()) && !e.isAfter(h.close())) {
                    date = d;
                    bookedStart = s;
                    bookedEnd = e;
                    break;
                }
            }
            if (date == null) {
                c.skip("no single branch-B tyre-bay booking inside " + from() + ".." + to()
                        + " — reseed the DB (V56 anchors bookings to CURRENT_DATE) to restore this check");
                return;
            }

            Hours h = hours.get(date.getDayOfWeek());
            int duration = 40;
            JsonNode b = json(slotsUrl(BRANCH_B, String.valueOf(SVC_TYRE_B), date, date));
            c.eq("day count", 1, b.get("days").size());
            JsonNode day = day(b, date.toString());
            shape(c, day, date.toString(), duration);

            int expectedMorning = fits(h.open(), bookedStart, duration);
            int expectedAfternoon = fits(bookedEnd, h.close(), duration);
            c.eq("total slots", expectedMorning + expectedAfternoon, count(day));

            int morning = 0;
            int afternoon = 0;
            for (JsonNode s : day.get("slots")) {
                LocalTime st = LocalTime.parse(s.get("startTime").asText());
                LocalTime en = LocalTime.parse(s.get("endTime").asText());
                if (st.isBefore(bookedEnd) && en.isAfter(bookedStart)) {
                    c.fails.add("slot " + st + "-" + en + " overlaps the booked [" + bookedStart + "," + bookedEnd + ") window");
                }
                if (st.isBefore(bookedStart)) {
                    morning++;
                } else {
                    afternoon++;
                }
            }
            c.eq("slots before the booking", expectedMorning, morning);
            c.eq("slots after the booking", expectedAfternoon, afternoon);
            if (expectedMorning > 0) {
                c.eq("first", spanOf(h.open(), duration), first(day));
            }
            if (expectedAfternoon > 0) {
                c.eq("last", spanOf(lastStart(bookedEnd, h.close(), duration), duration), last(day));
            }
            if (expectedMorning > 0 && expectedAfternoon > 0) {
                int lastMorningIdx = expectedMorning - 1;
                c.eq(
                        "last slot before the booking",
                        spanOf(lastStart(h.open(), bookedStart, duration), duration),
                        span(day, lastMorningIdx));
                c.eq("next slot after the booking", spanOf(bookedEnd, duration), span(day, lastMorningIdx + 1));
            }
            c.note("booking " + date + " " + bookedStart + "-" + bookedEnd + " excluded; "
                    + expectedMorning + " + " + expectedAfternoon + " slots either side");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 7. from = today -> nothing in the past, later days untruncated. */
    private void check7() throws Exception {
        Chk c = check("7", "from = today");
        try {
            LocalDateTime now = LocalDateTime.now(WARSAW);
            LocalDate today = today();
            LocalDate plus2 = today.plusDays(2);
            LocalTime ceil = ceilTo15(now.toLocalTime());
            c.note("ran at " + now.toLocalTime().withNano(0) + " Warsaw, grid ceiling " + ceil);

            Map<DayOfWeek, Hours> hours = hours(BRANCH_A);
            Set<LocalDate> busy = busyDates(BRANCH_A, today, plus2);

            JsonNode b = json(slotsUrl(BRANCH_A, String.valueOf(SVC_OIL), today, plus2));
            c.eq("day count", 3, b.get("days").size());
            JsonNode day0 = b.get("days").get(0);
            c.eq("day 0 date", today.toString(), day0.get("date").asText());

            // Today is legitimately empty when the branch is closed, when the remaining window no
            // longer fits the visit (after close, and after 13:00 on the short Saturday), or when
            // the branch is fully booked for the rest of the day.
            Hours h0 = hours.get(today.getDayOfWeek());
            LocalTime earliestToday = h0 == null ? null : (ceil.isAfter(h0.open()) ? ceil : h0.open());
            boolean roomLeftToday = h0 != null && fits(earliestToday, h0.close(), 60) > 0;
            if (count(day0) == 0) {
                c.ok(
                        "day 0 empty at " + now.toLocalTime().withNano(0) + " but the window "
                                + earliestToday + "-" + (h0 == null ? "closed" : h0.close()) + " still fits a 60-min visit",
                        !roomLeftToday || busy.contains(today));
                c.note("day 0 empty (branch closed, window too short from " + ceil + ", or fully booked)");
            } else {
                LocalTime firstStart = LocalTime.parse(day0.get("slots").get(0).get("startTime").asText());
                c.ok(
                        "day 0 first slot " + firstStart + " must not precede the grid ceiling " + ceil,
                        !firstStart.isBefore(ceil));
                c.ok(
                        "day 0 has slots but the branch is closed today",
                        earliestToday != null);
                if (earliestToday != null) {
                    c.ok(
                            "day 0 first slot " + firstStart + " must not precede opening time " + earliestToday,
                            !firstStart.isBefore(earliestToday));
                }
                c.note("day 0 first slot " + firstStart + (firstStart.equals(ceil) ? " (== ceiling)" : ""));
                shape(c, day0, today.toString(), 60);
            }

            for (int i = 1; i < b.get("days").size(); i++) {
                JsonNode d = b.get("days").get(i);
                LocalDate date = LocalDate.parse(d.get("date").asText());
                Hours h = hours.get(date.getDayOfWeek());
                if (count(d) == 0) {
                    c.ok(
                            "day " + i + " (" + date + ") empty but the branch is open " + h,
                            h == null || busy.contains(date));
                    continue;
                }
                c.eq(
                        "day " + i + " (" + date + ") not truncated",
                        h.open().toString(),
                        d.get("slots").get(0).get("startTime").asText());
            }
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private static LocalTime ceilTo15(LocalTime t) {
        int m = t.getMinute();
        int rem = m % GRID;
        if (rem == 0 && t.getSecond() == 0 && t.getNano() == 0) {
            return t;
        }
        LocalTime base = t.withSecond(0).withNano(0).minusMinutes(rem);
        return base.plusMinutes(GRID);
    }

    /** 8. Error paths a-k. */
    private void check8() {
        Chk c = check("8", "error paths a-k");
        LocalDate from = from();
        LocalDate next = from.plusDays(1);
        Map<String, String[]> cases = new LinkedHashMap<>();
        // key -> {query, expected status, expected code}
        cases.put("a 4 service ids", new String[] {
            slotsUrl(BRANCH_A, "11,27,7,43", from, next), "400", "MALFORMED_REQUEST"});
        cases.put("b from > to", new String[] {
            slotsUrl(BRANCH_A, "11", from.plusDays(2), from), "400", "INVALID_SLOT_QUERY"});
        cases.put("c 8-day span", new String[] {
            slotsUrl(BRANCH_A, "11", from, from.plusDays(7)), "400", "INVALID_SLOT_QUERY"});
        cases.put("d from in the past", new String[] {
            slotsUrl(BRANCH_A, "11", today().minusDays(3), today().minusDays(1)), "400", "INVALID_SLOT_QUERY"});
        cases.put("e malformed date", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=14-08-2026&to=" + next,
            "400", "MALFORMED_REQUEST"});
        cases.put("f unknown service id", new String[] {
            slotsUrl(BRANCH_A, "999999", from, next), "404", "SERVICE_NOT_FOUND"});
        cases.put("g unknown branch", new String[] {
            slotsUrl("99999999-9999-4999-8999-999999999999", "11", from, next), "404", "BRANCH_NOT_FOUND"});
        cases.put("h service of another branch", new String[] {
            slotsUrl(BRANCH_A, "14", from, next), "404", "SERVICE_NOT_FOUND"});
        cases.put("i duplicate service ids", new String[] {
            slotsUrl(BRANCH_A, "11,11", from, next), "400", "INVALID_SLOT_QUERY"});
        cases.put("j missing serviceIds", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?from=" + from + "&to=" + next, "400", "MALFORMED_REQUEST"});
        cases.put("k branchId not a UUID", new String[] {
            slotsUrl("not-a-uuid", "11", from, next), "400", "MALFORMED_REQUEST"});

        int okCount = 0;
        for (Map.Entry<String, String[]> e : cases.entrySet()) {
            String label = e.getKey();
            String path = e.getValue()[0];
            int expectedStatus = Integer.parseInt(e.getValue()[1]);
            String expectedCode = e.getValue()[2];
            int before = c.fails.size();
            try {
                ResponseEntity<String> r = raw(path);
                c.eq(label + " status", expectedStatus, r.getStatusCode().value());
                JsonNode body = mapper.readTree(r.getBody());
                for (String f : List.of("type", "title", "status", "detail", "instance", "code")) {
                    c.ok(label + " body missing '" + f + "' (got " + body.toString() + ")", body.has(f));
                }
                if (body.has("code")) {
                    c.eq(label + " code", expectedCode, body.get("code").asText());
                }
                if (body.has("instance")) {
                    String expectedInstance = path.substring(0, path.indexOf('?') < 0 ? path.length() : path.indexOf('?'));
                    c.eq(label + " instance", expectedInstance, body.get("instance").asText());
                }
                if (body.has("detail")) {
                    String detail = body.get("detail").asText();
                    c.ok(
                            label + " detail leaks internals: " + detail,
                            !detail.contains("\tat ")
                                    && !detail.contains("com.hutnyk")
                                    && !detail.toLowerCase().contains("select ")
                                    && !detail.contains("org.springframework")
                                    && !detail.contains("Exception:"));
                }
            } catch (Throwable t) {
                c.fails.add(label + " threw: " + t);
            }
            if (c.fails.size() == before) {
                okCount++;
            }
        }
        c.note(okCount + "/" + cases.size() + " error cases exact (status + code + problem fields + instance)");
    }

    /** 9. No authentication anywhere. */
    private void check9() {
        Chk c = check("9", "no auth");
        try {
            LocalDate from = from();
            LocalDate next = from.plusDays(1);
            List<String> paths =
                    List.of(
                            slotsUrl(BRANCH_A, "11", from, next),
                            slotsUrl(BRANCH_A, "11", from.plusDays(2), from),
                            slotsUrl("99999999-9999-4999-8999-999999999999", "11", from, next));
            List<Integer> statuses = new ArrayList<>();
            for (String p : paths) {
                ResponseEntity<String> r = raw(p);
                int s = r.getStatusCode().value();
                statuses.add(s);
                c.ok("got " + s + " (auth wall) for " + p, s != 401 && s != 403);
                List<String> setCookie = r.getHeaders().get("Set-Cookie");
                if (setCookie != null) {
                    for (String sc : setCookie) {
                        c.ok("forced JSESSIONID cookie: " + sc, !sc.contains("JSESSIONID"));
                    }
                }
            }
            c.eq("statuses", List.of(200, 400, 404), statuses);
            c.note("no cookie sent; statuses 200/400/404, never 401/403, no Set-Cookie: JSESSIONID");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }
}
