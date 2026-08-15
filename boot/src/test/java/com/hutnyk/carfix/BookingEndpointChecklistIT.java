package com.hutnyk.carfix;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/**
 * M5 checklist: the roadmap's "done when" loop over real HTTP against the seeded dev DB.
 * Every booking it creates is cancelled and deleted again in {@code cleanup()}, so re-runs
 * start from the same state. Window = tomorrow .. +6 days, first open day with a slot.
 */
@ActiveProfiles("dev")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
            "logging.level.root=WARN",
            "logging.level.org.hibernate.SQL=WARN",
            "logging.level.org.hibernate.orm.jdbc.bind=WARN"
        })
class BookingEndpointChecklistIT {

    private static final String BRANCH_A = "10000000-0000-4000-8000-000000000001";
    private static final String CAR_1 = "20000000-0000-4000-8000-000000000001";
    private static final String CAR_FOREIGN = "20000000-0000-4000-8000-000000000004";
    private static final int SVC_OIL = 11;
    private static final int SVC_BRAKES = 27;
    private static final int SVC_OTHER_BRANCH = 14;
    private static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");

    @LocalServerPort private int port;
    @Autowired private TestRestTemplate rest;
    @Autowired private JdbcTemplate jdbc;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Chk> report = new ArrayList<>();
    private final List<String> createdBookingIds = new ArrayList<>();
    private List<String> bookingsOfCarBefore = List.of();
    private Timestamp runStart;
    private boolean snapshotTaken;
    private String cookie;

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

        String verdict() {
            if (!fails.isEmpty()) {
                return "**FAIL**";
            }
            return skipped != null ? "SKIP" : "PASS";
        }

        List<String> shown() {
            if (!fails.isEmpty()) {
                return fails;
            }
            List<String> out = new ArrayList<>();
            if (skipped != null) {
                out.add("SKIPPED: " + skipped);
            }
            out.addAll(notes);
            return out;
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

    private HttpHeaders authed() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        if (cookie != null) {
            h.add(HttpHeaders.COOKIE, cookie);
        }
        return h;
    }

    private ResponseEntity<String> get(String path) {
        return rest.exchange(url(path), HttpMethod.GET, new HttpEntity<>(authed()), String.class);
    }

    private ResponseEntity<String> post(String path, String body) {
        return rest.exchange(url(path), HttpMethod.POST, new HttpEntity<>(body, authed()), String.class);
    }

    private JsonNode json(ResponseEntity<String> r) throws Exception {
        return mapper.readTree(r.getBody());
    }

    private void login() throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> r = rest.exchange(url("/api/auth/login"), HttpMethod.POST,
                new HttpEntity<>("{\"email\":\"test@gmail.com\",\"password\":\"Qwerty1!\"}", h), String.class);
        if (r.getStatusCode().value() != 200) {
            throw new AssertionError("login failed: " + r.getStatusCode() + " " + r.getBody());
        }
        String setCookie = String.join("; ", Objects.requireNonNull(r.getHeaders().get(HttpHeaders.SET_COOKIE)));
        int start = setCookie.indexOf("JSESSIONID=");
        if (start < 0) {
            throw new AssertionError("login set no JSESSIONID cookie: " + setCookie);
        }
        int end = setCookie.indexOf(';', start);
        cookie = setCookie.substring(start, end < 0 ? setCookie.length() : end);
    }

    private static String bookingBody(String branchId, String carProfileId, String serviceIds,
                                      String date, String startTime) {
        return "{\"branchId\":\"" + branchId + "\",\"carProfileId\":\"" + carProfileId + "\","
                + "\"serviceIds\":[" + serviceIds + "],\"date\":\"" + date + "\",\"startTime\":\"" + startTime + "\"}";
    }

    private JsonNode slots(int serviceId, LocalDate from, LocalDate to) throws Exception {
        return slots(String.valueOf(serviceId), from, to);
    }

    private JsonNode slots(String serviceIds, LocalDate from, LocalDate to) throws Exception {
        ResponseEntity<String> r = get("/api/branches/" + BRANCH_A + "/slots?serviceIds=" + serviceIds
                + "&from=" + from + "&to=" + to);
        if (r.getStatusCode().value() != 200) {
            throw new AssertionError("slots " + r.getStatusCode() + " " + r.getBody());
        }
        return json(r);
    }

    private static List<String> starts(JsonNode day) {
        List<String> out = new ArrayList<>();
        day.get("slots").forEach(s -> out.add(s.get("startTime").asText()));
        return out;
    }

    private static JsonNode firstOpenDay(JsonNode body) {
        for (JsonNode d : body.get("days")) {
            if (d.get("slots").size() > 0) {
                return d;
            }
        }
        return null;
    }

    private static JsonNode day(JsonNode body, String date) {
        for (JsonNode d : body.get("days")) {
            if (date.equals(d.get("date").asText())) {
                return d;
            }
        }
        throw new AssertionError("day " + date + " missing");
    }

    private int count(String table, String bookingId) {
        return Objects.requireNonNull(jdbc.queryForObject(
                "SELECT count(*) FROM " + schema + "." + table + " WHERE booking_id = ?::uuid", Integer.class, bookingId));
    }

    /** One occupancy row is written per requirement row, per segment — so this is the expected count. */
    private int requirements(String table, int serviceId) {
        return Objects.requireNonNull(jdbc.queryForObject(
                "SELECT count(*) FROM " + schema + "." + table + " WHERE service_id = ?", Integer.class, serviceId));
    }

    /**
     * The ids the sweep is allowed to consider: this car's bookings created no earlier than the run
     * itself. {@code bookings.created_at} defaults to the DB's own {@code now()}, so the bound is
     * read from the DB too — no JVM/DB clock skew.
     */
    private List<String> bookingIdsOfCarSince(String carProfileId, Timestamp since) {
        return jdbc.queryForList(
                "SELECT booking_id::text FROM " + schema
                        + ".bookings WHERE car_profile_id = ?::uuid AND created_at >= ?",
                String.class, carProfileId, since);
    }

    /**
     * Must succeed before anything is booked: the sweep in {@code cleanup()} deletes what is NOT in
     * this snapshot, so a missing snapshot would make it delete the seeded bookings of this car.
     */
    private void takeSnapshot() {
        runStart = Objects.requireNonNull(jdbc.queryForObject("SELECT localtimestamp", Timestamp.class));
        bookingsOfCarBefore = jdbc.queryForList(
                "SELECT booking_id::text FROM " + schema + ".bookings WHERE car_profile_id = ?::uuid",
                String.class, CAR_1);
        snapshotTaken = true;
    }

    private void remember(JsonNode created) {
        createdBookingIds.add(created.get("bookingId").asText());
    }

    /**
     * The remembered ids cover the normal path; the diff against the pre-run snapshot also catches a
     * booking whose 201 never reached the client, which would otherwise hold its slot forever. The
     * diff runs only when that snapshot was actually taken — without it, "not in the snapshot" would
     * mean "every booking of this car", seeded ones included.
     */
    private void cleanup() {
        Set<String> ids = new LinkedHashSet<>(createdBookingIds);
        if (snapshotTaken) {
            try {
                List<String> appeared = new ArrayList<>(bookingIdsOfCarSince(CAR_1, runStart));
                appeared.removeAll(bookingsOfCarBefore);
                ids.addAll(appeared);
            } catch (RuntimeException e) {
                System.out.println("\nsweep query failed, removing the remembered ids only: " + e);
            }
        } else {
            System.out.println("\nno pre-run snapshot — sweep skipped, only the remembered ids are removed");
        }
        for (String id : ids) {
            post("/api/customer/bookings/" + id + "/cancel", "");
            for (String t : List.of("service_bays_bookings", "employees_bookings", "equipment_bookings",
                    "bookings_services")) {
                jdbc.update("DELETE FROM " + schema + "." + t + " WHERE booking_id = ?::uuid", id);
            }
            jdbc.update("DELETE FROM " + schema + ".bookings WHERE booking_id = ?::uuid", id);
        }
    }

    // ------------------------------------------------------------------ checks

    @Test
    void checklist() throws Exception {
        takeSnapshot();
        try {
            login();
            LocalDate from = LocalDate.now(WARSAW).plusDays(1);
            LocalDate to = from.plusDays(6);
            check1to4(from, to);
            check5(from, to);
            check6(from);
            check7();
            check8(from, to);
        } finally {
            try {
                cleanup();
            } catch (RuntimeException e) {
                System.out.println("\ncleanup failed, the dev DB may hold leftovers: " + e);
            }
            StringBuilder sb = new StringBuilder("\n\n| # | check | result | notes |\n|---|---|---|---|\n");
            for (Chk c : report) {
                sb.append("| ").append(c.id).append(" | ").append(c.name).append(" | ").append(c.verdict())
                        .append(" | ").append(String.join("; ", c.shown())).append(" |\n");
            }
            System.out.println(sb);
        }
        long failed = report.stream().filter(c -> !c.fails.isEmpty()).count();
        if (failed > 0) {
            throw new AssertionError(failed + " checklist item(s) failed — see the table above");
        }
    }

    /** 1 book a listed slot → 201; 2 rows written; 3 slot listing shrinks or holds; 4 My Bookings shows it. */
    private void check1to4(LocalDate from, LocalDate to) throws Exception {
        Chk c1 = check("1", "book a listed slot → 201 with the card");
        Chk c2 = check("2", "segment + occupancy rows written with price snapshot");
        Chk c3 = check("3", "slot listing after booking ⊆ before");
        Chk c4 = check("4", "booking appears in My Bookings, then cancel frees the slot");
        JsonNode before = slots(SVC_OIL, from, to);
        JsonNode day = firstOpenDay(before);
        if (day == null) {
            for (Chk c : List.of(c1, c2, c3, c4)) {
                c.skipped = "no open day with a slot in the window — reseed the DB";
            }
            return;
        }
        String date = day.get("date").asText();
        List<String> startsBefore = starts(day);
        String start = startsBefore.getFirst();

        ResponseEntity<String> r = post("/api/customer/bookings", bookingBody(BRANCH_A, CAR_1, "" + SVC_OIL, date, start));
        c1.eq("status", 201, r.getStatusCode().value());
        if (r.getStatusCode().value() != 201) {
            c1.fails.add("body: " + r.getBody());
            for (Chk c : List.of(c2, c3, c4)) {
                c.skipped = "nothing was booked — see check 1";
            }
            return;
        }
        JsonNode created = json(r);
        remember(created);
        String id = created.get("bookingId").asText();
        c1.eq("status field", "SCHEDULED", created.get("status").asText());
        c1.eq("date", date, created.get("date").asText());
        c1.ok("startTime starts with " + start, created.get("startTime").asText().startsWith(start));
        c1.eq("services size", 1, created.get("services").size());
        JsonNode firstService = created.path("services").path(0);
        c1.ok("services[0].name non-blank (got " + firstService.path("name") + ")",
                !firstService.path("name").asText().isBlank());
        c1.ok("services[0].price > 0 (got " + firstService.path("price") + ")",
                firstService.path("price").decimalValue().signum() > 0);
        c1.ok("totalPrice > 0 (got " + created.path("totalPrice") + ")",
                created.path("totalPrice").decimalValue().signum() > 0);
        c1.ok("reference BK-", created.get("reference").asText().startsWith("BK-"));

        String seededPrice = jdbc.queryForObject("SELECT price FROM " + schema + ".services WHERE service_id = ?",
                String.class, SVC_OIL);
        c2.eq("bookings_services rows", 1, count("bookings_services", id));
        c2.eq("segment price = service price at booking time", seededPrice,
                jdbc.queryForObject("SELECT price FROM " + schema + ".bookings_services WHERE booking_id = ?::uuid",
                        String.class, id));
        c2.eq("bay rows", 1, count("service_bays_bookings", id));
        int employeeReqs = requirements("service_employee_requirements", SVC_OIL);
        int equipmentReqs = requirements("service_equipment_requirements", SVC_OIL);
        c2.eq("employee rows = employee requirement slots", employeeReqs, count("employees_bookings", id));
        c2.eq("equipment rows = equipment requirement slots", equipmentReqs, count("equipment_bookings", id));
        c2.note("bay 1, employees " + employeeReqs + ", equipment " + equipmentReqs + ", price " + seededPrice);

        List<String> startsDuring = starts(day(slots(SVC_OIL, from, to), date));
        c3.ok("no new starts appeared", startsBefore.containsAll(startsDuring));
        c3.note(startsBefore.size() + " → " + startsDuring.size() + " starts on " + date);

        ResponseEntity<String> mine = get("/api/customer/bookings");
        c4.eq("My Bookings status", 200, mine.getStatusCode().value());
        if (mine.getStatusCode().value() == 200) {
            boolean listed = false;
            for (JsonNode b : json(mine)) {
                listed |= id.equals(b.get("bookingId").asText());
            }
            c4.ok("new booking listed in My Bookings", listed);
        }
        ResponseEntity<String> cancel = post("/api/customer/bookings/" + id + "/cancel", "");
        c4.eq("cancel status", 200, cancel.getStatusCode().value());
        if (cancel.getStatusCode().value() == 200) {
            c4.eq("cancel body status field", "CANCELLED", json(cancel).get("status").asText());
        }
        c4.eq("occupancy freed (bay)", 0, count("service_bays_bookings", id));
        c4.eq("occupancy freed (employees)", 0, count("employees_bookings", id));
        c4.eq("occupancy freed (equipment)", 0, count("equipment_bookings", id));
        c4.eq("segments kept as history", 1, count("bookings_services", id));
        List<String> startsAfter = starts(day(slots(SVC_OIL, from, to), date));
        c4.eq("slot listing restored after cancel", startsBefore, startsAfter);
    }

    /** 5 concurrent double-book: bays+1 parallel POSTs for one slot → ≥1 201, ≥1 409, never 5xx. */
    private void check5(LocalDate from, LocalDate to) throws Exception {
        Chk c = check("5", "parallel double-book → exactly the bay count succeeds at most, rest 409 SLOT_NOT_AVAILABLE");
        JsonNode day = firstOpenDay(slots(SVC_OIL, from, to));
        if (day == null) {
            c.skipped = "no open day";
            return;
        }
        String date = day.get("date").asText();
        String start = starts(day).getFirst();
        int bays = Objects.requireNonNull(jdbc.queryForObject(
                "SELECT count(*) FROM " + schema + ".service_bays WHERE branch_id = ?::uuid AND status = 'ACTIVE'",
                Integer.class, BRANCH_A));
        int n = bays + 1;
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<ResponseEntity<String>>> futures = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            futures.add(pool.submit(() -> {
                go.await();
                return post("/api/customer/bookings", bookingBody(BRANCH_A, CAR_1, "" + SVC_OIL, date, start));
            }));
        }
        go.countDown();
        int created = 0;
        int conflicts = 0;
        List<Integer> statuses = new ArrayList<>();
        try {
            for (Future<ResponseEntity<String>> f : futures) {
                ResponseEntity<String> r;
                try {
                    r = f.get();
                } catch (ExecutionException e) {
                    c.fails.add("request never completed: " + e.getCause());
                    continue;
                }
                statuses.add(r.getStatusCode().value());
                if (r.getStatusCode().value() == 201) {
                    created++;
                    remember(json(r));
                } else if (r.getStatusCode().value() == 409) {
                    conflicts++;
                    c.eq("409 code", "SLOT_NOT_AVAILABLE", json(r).get("code").asText());
                } else {
                    c.fails.add("unexpected status " + r.getStatusCode().value() + ": " + r.getBody());
                }
            }
        } finally {
            pool.shutdown();
        }
        c.ok("at least one 201", created >= 1);
        c.ok("at least one 409", conflicts >= 1);
        c.ok("created ≤ active bays (" + bays + ")", created <= bays);
        c.note(n + " parallel POSTs for " + date + " " + start + " → statuses " + statuses);
    }

    /** 6 error contract. */
    private void check6(LocalDate from) throws Exception {
        Chk c = check("6", "error cases: status + code + problem fields");
        String d = from.toString();
        Map<String, String[]> cases = Map.of(
                "a off-grid start", new String[] {bookingBody(BRANCH_A, CAR_1, "11", d, "10:20"), "400", "INVALID_BOOKING_REQUEST"},
                "b four services", new String[] {bookingBody(BRANCH_A, CAR_1, "11,27,1,4", d, "10:00"), "400", "VALIDATION_FAILED"},
                "c foreign car profile", new String[] {bookingBody(BRANCH_A, CAR_FOREIGN, "11", d, "10:00"), "404", "CAR_PROFILE_NOT_FOUND"},
                "d unknown branch", new String[] {bookingBody("99999999-9999-4999-8999-999999999999", CAR_1, "11", d, "10:00"), "404", "BRANCH_NOT_FOUND"},
                "e service of another branch", new String[] {bookingBody(BRANCH_A, CAR_1, "" + SVC_OTHER_BRANCH, d, "10:00"), "404", "SERVICE_NOT_FOUND"},
                "f past start", new String[] {bookingBody(BRANCH_A, CAR_1, "11", from.minusDays(2).toString(), "10:00"), "409", "SLOT_NOT_AVAILABLE"});
        int okCount = 0;
        for (Map.Entry<String, String[]> e : cases.entrySet()) {
            String label = e.getKey();
            int before = c.fails.size();
            ResponseEntity<String> r = post("/api/customer/bookings", e.getValue()[0]);
            c.eq(label + " status", e.getValue()[1], String.valueOf(r.getStatusCode().value()));
            JsonNode body = json(r);
            for (String f : List.of("type", "title", "status", "detail", "instance", "code")) {
                c.ok(label + " body missing '" + f + "'", body.has(f));
            }
            if (body.has("code")) {
                c.eq(label + " code", e.getValue()[2], body.get("code").asText());
            }
            if (label.startsWith("a")) {
                c.ok("a errors.startTime present", body.has("errors") && body.get("errors").has("startTime"));
            }
            if (r.getStatusCode().value() == 201) {
                remember(body);
            }
            if (c.fails.size() == before) {
                okCount++;
            }
        }
        c.note(okCount + "/" + cases.size() + " exact");
    }

    /** 7 auth wall. */
    private void check7() {
        Chk c = check("7", "no session → 401, never 500");
        String saved = cookie;
        cookie = null;
        ResponseEntity<String> r = post("/api/customer/bookings",
                bookingBody(BRANCH_A, CAR_1, "11", LocalDate.now(WARSAW).plusDays(1).toString(), "10:00"));
        cookie = saved;
        c.eq("status", 401, r.getStatusCode().value());
    }

    /**
     * 8 the multi-segment path: two services in one visit, so two {@code bookings_services} rows share
     * one booking, the per-segment employee rows are written per service, and the card is assembled
     * from a re-read that fetches a collection.
     */
    private void check8(LocalDate from, LocalDate to) throws Exception {
        Chk c = check("8", "two-service visit → one booking, two segments, one bay");
        String pair = SVC_OIL + "," + SVC_BRAKES;
        JsonNode body = slots(pair, from, to);
        if (!body.get("chainable").asBoolean()) {
            c.skipped = "services " + pair + " share no bay type — reseed the DB";
            return;
        }
        JsonNode day = firstOpenDay(body);
        if (day == null) {
            c.skipped = "no open day with a slot for " + pair + " in the window — reseed the DB";
            return;
        }
        String date = day.get("date").asText();
        String start = starts(day).getFirst();

        ResponseEntity<String> r = post("/api/customer/bookings", bookingBody(BRANCH_A, CAR_1, pair, date, start));
        c.eq("status", 201, r.getStatusCode().value());
        if (r.getStatusCode().value() != 201) {
            c.fails.add("body: " + r.getBody());
            return;
        }
        JsonNode created = json(r);
        remember(created);
        String id = created.get("bookingId").asText();
        c.eq("services size", 2, created.get("services").size());
        c.ok("totalPrice > 0 (got " + created.path("totalPrice") + ")",
                created.path("totalPrice").decimalValue().signum() > 0);
        c.eq("bookings_services rows", 2, count("bookings_services", id));
        c.eq("bay rows (one for the whole visit)", 1, count("service_bays_bookings", id));
        int employeeReqs = requirements("service_employee_requirements", SVC_OIL)
                + requirements("service_employee_requirements", SVC_BRAKES);
        c.eq("employee rows = both segments' requirement slots", employeeReqs, count("employees_bookings", id));
        c.note("booked " + pair + " on " + date + " " + start + ", employees " + employeeReqs);
    }
}
