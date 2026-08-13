package com.hutnyk.carfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

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

    private static final List<String> WEEK =
            List.of(
                    "2026-08-14", "2026-08-15", "2026-08-16", "2026-08-17", "2026-08-18",
                    "2026-08-19", "2026-08-20");
    private static final List<String> WEEKDAYS =
            List.of("2026-08-14", "2026-08-17", "2026-08-18", "2026-08-19", "2026-08-20");

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Chk> report = new ArrayList<>();

    // ---------------------------------------------------------------- harness

    private static final class Chk {
        final String id;
        final String name;
        final List<String> fails = new ArrayList<>();
        final List<String> notes = new ArrayList<>();

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

        boolean passed() {
            return fails.isEmpty();
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
            if (st.getMinute() % 15 != 0 || st.getSecond() != 0) {
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
                if (step <= 0 || step % 15 != 0) {
                    c.fails.add(date + ": step " + prev + " -> " + st + " is not a positive multiple of 15 min");
                }
            }
            prev = st;
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
        sb.append("| # | check | pass / fail | notes |\n|---|---|---|---|\n");
        for (Chk c : report) {
            String notes = String.join("; ", c.passed() ? c.notes : c.fails);
            sb.append("| ").append(c.id).append(" | ").append(c.name).append(" | ")
                    .append(c.passed() ? "PASS" : "**FAIL**").append(" | ").append(notes).append(" |\n");
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
            JsonNode b = json("/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-14&to=2026-08-20");
            c.eq("chainable", true, b.get("chainable").asBoolean());
            c.eq("day count", 7, b.get("days").size());
            c.eq("dates in order", WEEK, dates(b));

            for (String d : WEEKDAYS) {
                JsonNode day = day(b, d);
                c.eq(d + " slot count", 37, count(day));
                c.eq(d + " first", "08:00-09:00", first(day));
                c.eq(d + " last", "17:00-18:00", last(day));
                shape(c, day, d, 60);
            }
            JsonNode sat = day(b, "2026-08-15");
            c.eq("Sat count", 17, count(sat));
            c.eq("Sat first", "09:00-10:00", first(sat));
            c.eq("Sat last", "13:00-14:00", last(sat));
            shape(c, sat, "2026-08-15", 60);

            c.eq("Sun 08-16 empty", 0, count(day(b, "2026-08-16")));
            c.note("7 days, Mon-Fri 37x60min 08:00-18:00, Sat 17, Sun [] present");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 2. Engine replacement — the greedy-matching trap. */
    private void check2() throws Exception {
        Chk c = check("2", "engine replacement (greedy trap)");
        try {
            JsonNode b = json("/api/branches/" + BRANCH_A + "/slots?serviceIds=1&from=2026-08-14&to=2026-08-20");
            c.eq("day count", 7, b.get("days").size());
            for (String d : WEEKDAYS) {
                JsonNode day = day(b, d);
                c.ok(d + " must be NON-EMPTY (Kuhn augmenting path regressed)", count(day) > 0);
                c.eq(d + " slot count", 9, count(day));
                c.eq(d + " first", "08:00-16:00", first(day));
                c.eq(d + " last", "10:00-18:00", last(day));
                shape(c, day, d, 480);
            }
            c.eq("Sat empty", 0, count(day(b, "2026-08-15")));
            c.eq("Sun empty", 0, count(day(b, "2026-08-16")));
            c.note("Mon-Fri non-empty: 9x480min 08:00-16:00..10:00-18:00; Sat/Sun []");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 3. Clutch replacement — two trolley jacks drawn from one pool of two. */
    private void check3() throws Exception {
        Chk c = check("3", "clutch replacement (two jacks)");
        try {
            JsonNode b = json("/api/branches/" + BRANCH_A + "/slots?serviceIds=4&from=2026-08-14&to=2026-08-20");
            for (String d : WEEKDAYS) {
                JsonNode day = day(b, d);
                c.ok(d + " must be NON-EMPTY (both jack requirements hit the same unit)", count(day) > 0);
                c.eq(d + " slot count", 21, count(day));
                c.eq(d + " first", "08:00-13:00", first(day));
                c.eq(d + " last", "13:00-18:00", last(day));
                shape(c, day, d, 300);
            }
            JsonNode sat = day(b, "2026-08-15");
            c.eq("Sat count (exact-fit boundary)", 1, count(sat));
            c.eq("Sat only slot", "09:00-14:00", first(sat));
            c.eq("Sun empty", 0, count(day(b, "2026-08-16")));
            c.note("Mon-Fri 21x300min; Sat exactly 1 (09:00-14:00 exact fit); Sun []");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 4. Disjoint bay types -> chainable:false, 200, all days empty. */
    private void check4() throws Exception {
        Chk c = check("4", "disjoint bay types -> chainable false");
        try {
            ResponseEntity<String> r =
                    raw("/api/branches/" + BRANCH_A + "/slots?serviceIds=43,4&from=2026-08-14&to=2026-08-20");
            c.eq("status", 200, r.getStatusCode().value());
            JsonNode b = mapper.readTree(r.getBody());
            c.eq("chainable", false, b.get("chainable").asBoolean());
            c.eq("day count", 7, b.get("days").size());
            c.eq("dates", WEEK, dates(b));
            for (String d : WEEK) {
                c.eq(d + " empty", 0, count(day(b, d)));
            }

            JsonNode alt = json("/api/branches/" + BRANCH_A + "/slots?serviceIds=33,1&from=2026-08-14&to=2026-08-20");
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
            JsonNode b = json("/api/branches/" + BRANCH_A + "/slots?serviceIds=11,27&from=2026-08-14&to=2026-08-20");
            c.eq("chainable", true, b.get("chainable").asBoolean());
            for (String d : WEEKDAYS) {
                JsonNode day = day(b, d);
                c.eq(d + " slot count", 31, count(day));
                c.eq(d + " first", "08:00-10:30", first(day));
                c.eq(d + " last", "15:30-18:00", last(day));
                shape(c, day, d, 150);
            }
            JsonNode sat = day(b, "2026-08-15");
            c.eq("Sat count", 11, count(sat));
            c.eq("Sat first", "09:00-11:30", first(sat));
            c.eq("Sat last", "11:30-14:00", last(sat));
            shape(c, sat, "2026-08-15", 150);
            c.eq("Sun empty", 0, count(day(b, "2026-08-16")));
            c.note("chainable:true, every slot exactly 150 min (60+90, no gap leak); Mon-Fri 31, Sat 11");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 6. A booked window (Bay 4, 12:00-14:00 on 2026-08-18) must be excluded. */
    private void check6() throws Exception {
        Chk c = check("6", "booked window excluded");
        try {
            JsonNode b = json("/api/branches/" + BRANCH_B + "/slots?serviceIds=35&from=2026-08-18&to=2026-08-18");
            c.eq("day count", 1, b.get("days").size());
            JsonNode day = day(b, "2026-08-18");
            c.eq("total slots", 28, count(day));
            shape(c, day, "2026-08-18", 40);

            int morning = 0;
            int afternoon = 0;
            for (JsonNode s : day.get("slots")) {
                LocalTime st = LocalTime.parse(s.get("startTime").asText());
                LocalTime en = LocalTime.parse(s.get("endTime").asText());
                boolean overlapsBooking = st.isBefore(LocalTime.of(14, 0)) && en.isAfter(LocalTime.of(12, 0));
                if (overlapsBooking) {
                    c.fails.add("slot " + st + "-" + en + " overlaps the booked [12:00,14:00) window");
                }
                if (st.isBefore(LocalTime.of(12, 0))) {
                    morning++;
                } else {
                    afternoon++;
                }
            }
            c.eq("morning slots", 14, morning);
            c.eq("afternoon slots", 14, afternoon);
            c.eq("first", "08:00-08:40", first(day));
            c.eq("last", "17:15-17:55", last(day));

            List<String> starts = new ArrayList<>();
            day.get("slots").forEach(s -> starts.add(s.get("startTime").asText()));
            int lastMorningIdx = -1;
            for (int i = 0; i < starts.size(); i++) {
                if (LocalTime.parse(starts.get(i)).isBefore(LocalTime.of(12, 0))) {
                    lastMorningIdx = i;
                }
            }
            c.eq("last morning slot", "11:15-11:55", span(day, lastMorningIdx));
            c.eq("next slot after the booking", "14:00-14:40", span(day, lastMorningIdx + 1));
            c.note("28 slots (14+14), nothing between 11:30 and 13:45, gap 11:15-11:55 -> 14:00-14:40");
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    /** 7. from = today -> nothing in the past, later days untruncated. */
    private void check7() throws Exception {
        Chk c = check("7", "from = today");
        try {
            LocalDateTime now = LocalDateTime.now(WARSAW);
            LocalDate today = now.toLocalDate();
            LocalDate plus2 = today.plusDays(2);
            LocalTime ceil = ceilTo15(now.toLocalTime());
            c.note("ran at " + now.toLocalTime().withNano(0) + " Warsaw, grid ceiling " + ceil);

            JsonNode b =
                    json("/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=" + today + "&to=" + plus2);
            c.eq("day count", 3, b.get("days").size());
            JsonNode day0 = b.get("days").get(0);
            c.eq("day 0 date", today.toString(), day0.get("date").asText());

            boolean openToday =
                    today.getDayOfWeek().getValue() <= 6 && now.toLocalTime().isBefore(LocalTime.of(17, 0));
            if (count(day0) == 0) {
                c.ok("day 0 empty is only legitimate outside the bookable window", !openToday);
                c.note("day 0 empty (outside 08:00-17:00 window or closed day)");
            } else {
                LocalTime firstStart = LocalTime.parse(day0.get("slots").get(0).get("startTime").asText());
                c.ok(
                        "day 0 first slot " + firstStart + " must not precede the grid ceiling " + ceil,
                        !firstStart.isBefore(ceil));
                c.note("day 0 first slot " + firstStart + (firstStart.equals(ceil) ? " (== ceiling)" : ""));
                shape(c, day0, today.toString(), 60);
            }

            for (int i = 1; i < b.get("days").size(); i++) {
                JsonNode d = b.get("days").get(i);
                LocalDate date = LocalDate.parse(d.get("date").asText());
                if (count(d) == 0) {
                    c.ok("day " + i + " (" + date + ") empty only if Sunday", date.getDayOfWeek().getValue() == 7);
                    continue;
                }
                String expectedFirst = date.getDayOfWeek().getValue() == 6 ? "09:00" : "08:00";
                c.eq(
                        "day " + i + " (" + date + ") not truncated",
                        expectedFirst,
                        d.get("slots").get(0).get("startTime").asText());
            }
        } catch (Throwable t) {
            c.fails.add("threw: " + t);
        }
    }

    private static LocalTime ceilTo15(LocalTime t) {
        int m = t.getMinute();
        int rem = m % 15;
        if (rem == 0 && t.getSecond() == 0 && t.getNano() == 0) {
            return t;
        }
        LocalTime base = t.withSecond(0).withNano(0).minusMinutes(rem);
        return base.plusMinutes(15);
    }

    /** 8. Error paths a-k. */
    private void check8() {
        Chk c = check("8", "error paths a-k");
        Map<String, String[]> cases = new LinkedHashMap<>();
        // key -> {query, expected status, expected code}
        cases.put("a 4 service ids", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11,27,7,43&from=2026-08-14&to=2026-08-15",
            "400", "MALFORMED_REQUEST"});
        cases.put("b from > to", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-16&to=2026-08-14",
            "400", "INVALID_SLOT_QUERY"});
        cases.put("c 8-day span", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-14&to=2026-08-21",
            "400", "INVALID_SLOT_QUERY"});
        cases.put("d from in the past", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-01&to=2026-08-03",
            "400", "INVALID_SLOT_QUERY"});
        cases.put("e malformed date", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=14-08-2026&to=2026-08-15",
            "400", "MALFORMED_REQUEST"});
        cases.put("f unknown service id", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=999999&from=2026-08-14&to=2026-08-15",
            "404", "SERVICE_NOT_FOUND"});
        cases.put("g unknown branch", new String[] {
            "/api/branches/99999999-9999-4999-8999-999999999999/slots?serviceIds=11&from=2026-08-14&to=2026-08-15",
            "404", "BRANCH_NOT_FOUND"});
        cases.put("h service of another branch", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=14&from=2026-08-14&to=2026-08-15",
            "404", "SERVICE_NOT_FOUND"});
        cases.put("i duplicate service ids", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11,11&from=2026-08-14&to=2026-08-15",
            "400", "INVALID_SLOT_QUERY"});
        cases.put("j missing serviceIds", new String[] {
            "/api/branches/" + BRANCH_A + "/slots?from=2026-08-14&to=2026-08-15",
            "400", "MALFORMED_REQUEST"});
        cases.put("k branchId not a UUID", new String[] {
            "/api/branches/not-a-uuid/slots?serviceIds=11&from=2026-08-14&to=2026-08-15",
            "400", "MALFORMED_REQUEST"});

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
            List<String> paths =
                    List.of(
                            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-14&to=2026-08-15",
                            "/api/branches/" + BRANCH_A + "/slots?serviceIds=11&from=2026-08-16&to=2026-08-14",
                            "/api/branches/99999999-9999-4999-8999-999999999999/slots?serviceIds=11&from=2026-08-14&to=2026-08-15");
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
