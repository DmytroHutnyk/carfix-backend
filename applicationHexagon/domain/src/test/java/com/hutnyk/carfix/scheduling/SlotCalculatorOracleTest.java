package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.user.UserId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Randomized cross-check of {@link SlotCalculator} against an independent brute-force oracle,
 * plus minute-set property tests for the TimeRange algebra and the bipartite matcher.
 */
class SlotCalculatorOracleTest {

    private static final int SEEDS = 3000;
    private static final int NO_MEMO_SEEDS = 300;
    private static final int ALGEBRA_SEEDS = 2000;
    private static final int MATCHER_SEEDS = 2000;

    private static final int GRID = 900;
    private static final int MAX_GAP = 900;
    private static final int DAY_SECONDS = 86400;
    private static final int GRID_POINTS = DAY_SECONDS / GRID + 1;
    private static final int WINDOW_FROM = 8 * 3600;
    private static final int WINDOW_TO = 14 * 3600;

    private static final LocalDateTime DAY = LocalDateTime.of(2026, 8, 14, 0, 0);
    private static final BranchId BRANCH_ID =
            BranchId.of(UUID.fromString("00000000-0000-0000-0000-0000000000ff"));

    private static final int[] DURATIONS = {15, 30, 40, 45, 50, 60, 90};
    private static final int[] BAY_TYPES = {1, 2};
    private static final int[] ROLES = {10, 11, 12};
    private static final int[] EQUIPMENT_TYPES = {20, 21};

    // ------------------------------------------------------------------ instance

    private record Instance(long seed, List<Service> services, List<BaySchedule> bays,
                            List<EmployeeSchedule> employees, List<EquipmentSchedule> equipment,
                            LocalDateTime notBefore) {

        int complexity() {
            int ranges = bays.stream().mapToInt(b -> b.free().size()).sum()
                    + employees.stream().mapToInt(e -> e.free().size()).sum()
                    + equipment.stream().mapToInt(e -> e.free().size()).sum();
            return services.size() * 1000 + bays.size() * 100 + employees.size() * 10
                    + equipment.size() * 10 + ranges;
        }
    }

    /**
     * Two profiles. "Sparse" resources get 0-3 arbitrary ranges (most instances are infeasible —
     * this stresses the rejection paths); "generous" resources get the whole 08:00-14:00 window
     * minus 0-2 busy chunks, which makes most instances yield real slots. Both profiles emit
     * overlapping / adjacent pieces in random order so the schedules' self-union is exercised.
     */
    private static Instance generate(long seed) {
        Random rnd = new Random(seed);
        boolean generous = rnd.nextInt(10) < 7;
        double p = generous ? 0.75 : 0.5;

        List<Service> services = new ArrayList<>();
        int serviceCount = 1 + rnd.nextInt(3);
        for (int i = 0; i < serviceCount; i++) {
            int id = i + 1;
            List<EmployeeRequirement> employeeReqs = new ArrayList<>();
            int employeeReqCount = 1 + rnd.nextInt(2);
            for (int r = 0; r < employeeReqCount; r++) {
                employeeReqs.add(EmployeeRequirement.of(
                        id * 100 + r, "emp-" + id + "-" + r, nonEmptySubset(rnd, ROLES, p)));
            }
            List<EquipmentRequirement> equipmentReqs = new ArrayList<>();
            int equipmentReqCount = rnd.nextInt(3);
            for (int r = 0; r < equipmentReqCount; r++) {
                equipmentReqs.add(EquipmentRequirement.of(id * 100 + 50 + r,
                        "eq-" + id + "-" + r, nonEmptySubset(rnd, EQUIPMENT_TYPES, p)));
            }
            int duration = generous
                    ? DURATIONS[rnd.nextInt(DURATIONS.length - serviceCount + 1)]
                    : DURATIONS[rnd.nextInt(DURATIONS.length)];
            services.add(Service.of(id, "S" + id, null, (short) duration, BigDecimal.TEN,
                    ServiceStatus.ACTIVE, BRANCH_ID, 1, nonEmptySubset(rnd, BAY_TYPES, p),
                    employeeReqs, equipmentReqs));
        }

        List<BaySchedule> bays = new ArrayList<>();
        int bayCount = 1 + rnd.nextInt(2);
        for (int i = 0; i < bayCount; i++) {
            bays.add(new BaySchedule(100 + i, BAY_TYPES[rnd.nextInt(BAY_TYPES.length)],
                    freeRanges(rnd, generous)));
        }

        List<EmployeeSchedule> employees = new ArrayList<>();
        int employeeCount = (generous ? 2 : 1) + rnd.nextInt(3);
        for (int i = 0; i < employeeCount; i++) {
            employees.add(new EmployeeSchedule(UserId.of(new UUID(0L, i + 1L)),
                    nonEmptySubset(rnd, ROLES, p), freeRanges(rnd, generous)));
        }

        List<EquipmentSchedule> equipment = new ArrayList<>();
        int equipmentCount = generous ? 1 + rnd.nextInt(3) : rnd.nextInt(4);
        for (int i = 0; i < equipmentCount; i++) {
            equipment.add(new EquipmentSchedule(500 + i,
                    EQUIPMENT_TYPES[generous ? i % EQUIPMENT_TYPES.length
                            : rnd.nextInt(EQUIPMENT_TYPES.length)],
                    freeRanges(rnd, generous)));
        }

        LocalDateTime notBefore = rnd.nextInt(10) < 7 ? DAY : atSeconds(rnd.nextInt(WINDOW_TO));
        return new Instance(seed, services, bays, employees, equipment, notBefore);
    }

    private static List<TimeRange> freeRanges(Random rnd, boolean generous) {
        List<int[]> pieces = new ArrayList<>();
        if (generous) {
            pieces.add(new int[]{WINDOW_FROM, WINDOW_TO});
            int busyCount = rnd.nextInt(3);
            for (int i = 0; i < busyCount; i++) {
                int from = boundary(rnd);
                int to = Math.min(WINDOW_TO, from + (1 + rnd.nextInt(6)) * GRID);
                pieces = cut(pieces, from, to);
            }
        } else {
            int count = rnd.nextInt(4);
            for (int i = 0; i < count; i++) {
                int a = boundary(rnd);
                int b = boundary(rnd);
                if (a != b) {
                    pieces.add(new int[]{Math.min(a, b), Math.max(a, b)});
                }
            }
        }

        List<TimeRange> out = new ArrayList<>();
        for (int[] piece : pieces) {
            int length = piece[1] - piece[0];
            if (length > 600 && rnd.nextInt(4) == 0) {
                int split = piece[0] + 60 * (1 + rnd.nextInt(length / 60 - 1));
                int overlap = rnd.nextInt(3) == 0 ? 0 : rnd.nextInt(300);
                out.add(TimeRange.of(atSeconds(piece[0]),
                        atSeconds(Math.min(piece[1], split + overlap))));
                out.add(TimeRange.of(atSeconds(split), atSeconds(piece[1])));
            } else {
                out.add(TimeRange.of(atSeconds(piece[0]), atSeconds(piece[1])));
            }
        }
        Collections.shuffle(out, rnd);
        return out;
    }

    private static List<int[]> cut(List<int[]> pieces, int from, int to) {
        List<int[]> out = new ArrayList<>();
        for (int[] piece : pieces) {
            if (to <= piece[0] || from >= piece[1]) {
                out.add(piece);
                continue;
            }
            if (piece[0] < from) {
                out.add(new int[]{piece[0], from});
            }
            if (to < piece[1]) {
                out.add(new int[]{to, piece[1]});
            }
        }
        return out;
    }

    private static int boundary(Random rnd) {
        int span = WINDOW_TO - WINDOW_FROM;
        return switch (rnd.nextInt(3)) {
            case 0 -> WINDOW_FROM + rnd.nextInt(span / GRID + 1) * GRID;
            case 1 -> WINDOW_FROM + rnd.nextInt(span / 60 + 1) * 60;
            default -> WINDOW_FROM + rnd.nextInt(span + 1);
        };
    }

    private static Set<Integer> nonEmptySubset(Random rnd, int[] pool, double probability) {
        Set<Integer> out = new LinkedHashSet<>();
        while (out.isEmpty()) {
            for (int v : pool) {
                if (rnd.nextDouble() < probability) {
                    out.add(v);
                }
            }
        }
        return out;
    }

    // ------------------------------------------------------------------ oracle

    private static final class Oracle {

        private final Instance inst;
        private final List<List<int[]>> bayFree = new ArrayList<>();
        private final List<List<int[]>> employeeFree = new ArrayList<>();
        private final List<List<int[]>> equipmentFree = new ArrayList<>();
        private final List<List<Service>> orders;
        private final int earliestStart;
        private final byte[] memo;

        Oracle(Instance inst, boolean useMemo) {
            this.inst = inst;
            inst.bays().forEach(b -> bayFree.add(unionSeconds(b.free())));
            inst.employees().forEach(e -> employeeFree.add(unionSeconds(e.free())));
            inst.equipment().forEach(e -> equipmentFree.add(unionSeconds(e.free())));
            this.orders = permutations(inst.services());
            this.earliestStart = ceilGrid(inst.notBefore());
            this.memo = useMemo ? new byte[inst.services().size() * GRID_POINTS] : null;
        }

        TreeSet<Integer> feasibleStarts() {
            TreeSet<Integer> feasible = new TreeSet<>();
            for (int start = 0; start < DAY_SECONDS; start += GRID) {
                if (start < earliestStart) {
                    continue;
                }
                if (feasibleAt(start)) {
                    feasible.add(start);
                }
            }
            return feasible;
        }

        boolean feasibleAt(int start) {
            for (int b = 0; b < inst.bays().size(); b++) {
                if (feasibleAtInBay(start, b)) {
                    return true;
                }
            }
            return false;
        }

        boolean feasibleAtInBay(int start, int bayIndex) {
            if (start < earliestStart || !acceptsAll(inst.services(), inst.bays().get(bayIndex))) {
                return false;
            }
            for (int[] window : bayFree.get(bayIndex)) {
                if (start < window[0] || start >= window[1]) {
                    continue;
                }
                for (List<Service> order : orders) {
                    if (place(order, 0, start, window[0], window[1])) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean place(List<Service> order, int index, int segmentStart, int low, int high) {
            Service service = order.get(index);
            int segmentEnd = segmentStart + service.getDurationMinutes() * 60;
            if (segmentStart < low || segmentEnd > high) {
                return false;
            }
            if (!segmentSatisfiable(service, segmentStart, segmentEnd)) {
                return false;
            }
            if (index == order.size() - 1) {
                return true;
            }
            for (int p = segmentEnd / GRID * GRID; p <= segmentEnd + MAX_GAP; p += GRID) {
                if (p >= segmentEnd && place(order, index + 1, p, low, high)) {
                    return true;
                }
            }
            return false;
        }

        boolean segmentSatisfiable(Service service, int from, int to) {
            int slot = -1;
            if (memo != null) {
                slot = inst.services().indexOf(service) * GRID_POINTS + from / GRID;
                if (memo[slot] != 0) {
                    return memo[slot] == 1;
                }
            }
            boolean result = computeSegment(service, from, to);
            if (memo != null) {
                memo[slot] = (byte) (result ? 1 : 2);
            }
            return result;
        }

        private boolean computeSegment(Service service, int from, int to) {
            List<List<Integer>> employeeCandidates = new ArrayList<>();
            for (EmployeeRequirement req : service.getEmployeeRequirements()) {
                List<Integer> candidates = new ArrayList<>();
                for (int i = 0; i < inst.employees().size(); i++) {
                    EmployeeSchedule e = inst.employees().get(i);
                    if (!Collections.disjoint(e.roleIds(), req.getRoleIds())
                            && coveredBy(employeeFree.get(i), from, to)) {
                        candidates.add(i);
                    }
                }
                employeeCandidates.add(candidates);
            }
            if (!hasSystemOfDistinctRepresentatives(employeeCandidates)) {
                return false;
            }
            List<List<Integer>> equipmentCandidates = new ArrayList<>();
            for (EquipmentRequirement req : service.getEquipmentRequirements()) {
                List<Integer> candidates = new ArrayList<>();
                for (int i = 0; i < inst.equipment().size(); i++) {
                    EquipmentSchedule e = inst.equipment().get(i);
                    if (req.getEquipmentTypeIds().contains(e.equipmentTypeId())
                            && coveredBy(equipmentFree.get(i), from, to)) {
                        candidates.add(i);
                    }
                }
                equipmentCandidates.add(candidates);
            }
            return hasSystemOfDistinctRepresentatives(equipmentCandidates);
        }
    }

    private static boolean acceptsAll(List<Service> services, BaySchedule bay) {
        return services.stream().allMatch(s -> s.getServiceBayTypeIds().contains(bay.bayTypeId()));
    }

    private static boolean coveredBy(List<int[]> free, int from, int to) {
        return free.stream().anyMatch(r -> r[0] <= from && to <= r[1]);
    }

    private static boolean hasSystemOfDistinctRepresentatives(List<List<Integer>> candidates) {
        return seat(candidates, 0, new HashSet<>());
    }

    private static boolean seat(List<List<Integer>> candidates, int slot, Set<Integer> used) {
        if (slot == candidates.size()) {
            return true;
        }
        for (Integer resource : candidates.get(slot)) {
            if (!used.add(resource)) {
                continue;
            }
            if (seat(candidates, slot + 1, used)) {
                return true;
            }
            used.remove(resource);
        }
        return false;
    }

    private static List<List<Service>> permutations(List<Service> services) {
        List<List<Service>> out = new ArrayList<>();
        permute(new ArrayList<>(services), new ArrayList<>(), out);
        return out;
    }

    private static void permute(List<Service> remaining, List<Service> prefix,
                                List<List<Service>> out) {
        if (remaining.isEmpty()) {
            out.add(List.copyOf(prefix));
            return;
        }
        for (int i = 0; i < remaining.size(); i++) {
            Service picked = remaining.remove(i);
            prefix.add(picked);
            permute(remaining, prefix, out);
            prefix.removeLast();
            remaining.add(i, picked);
        }
    }

    private static List<int[]> unionSeconds(List<TimeRange> ranges) {
        List<int[]> raw = new ArrayList<>();
        for (TimeRange r : ranges) {
            raw.add(new int[]{secondsOf(r.lower()), secondsOf(r.upper())});
        }
        raw.sort(Comparator.comparingInt(a -> a[0]));
        List<int[]> merged = new ArrayList<>();
        for (int[] r : raw) {
            if (!merged.isEmpty() && r[0] <= merged.getLast()[1]) {
                merged.getLast()[1] = Math.max(merged.getLast()[1], r[1]);
            } else {
                merged.add(new int[]{r[0], r[1]});
            }
        }
        return merged;
    }

    private static int secondsOf(LocalDateTime t) {
        return (int) ChronoUnit.SECONDS.between(DAY, t);
    }

    private static LocalDateTime atSeconds(int seconds) {
        return DAY.plusSeconds(seconds);
    }

    /** Smallest grid point (in seconds from day start) that is not before {@code t}. */
    private static int ceilGrid(LocalDateTime t) {
        int seconds = secondsOf(t);
        int grid = Math.floorDiv(seconds + GRID - 1, GRID) * GRID;
        if (grid == seconds && t.getNano() > 0) {
            grid += GRID;
        }
        return grid;
    }

    // ------------------------------------------------------------------ the cross-check

    @Test
    void oracle_cross_check_over_random_instances() {
        runCrossCheck(0, SEEDS, true);
    }

    @Test
    void oracle_cross_check_without_segment_memoisation() {
        runCrossCheck(0, NO_MEMO_SEEDS, false);
    }

    private void runCrossCheck(int fromSeed, int seedCount, boolean useMemo) {
        List<String> failures = new ArrayList<>();
        Instance worst = null;
        String worstReport = null;

        for (int seed = fromSeed; seed < fromSeed + seedCount; seed++) {
            Instance inst = generate(seed);
            Oracle oracle = new Oracle(inst, useMemo);
            TreeSet<Integer> expected = oracle.feasibleStarts();

            List<VisitPlan> plans = SlotCalculator.computeVisits(inst.services(), inst.bays(),
                    inst.employees(), inst.equipment(), inst.notBefore());
            TreeSet<Integer> actual = new TreeSet<>();
            plans.forEach(p -> actual.add(secondsOf(p.start())));

            List<String> problems = new ArrayList<>();
            if (!expected.equals(actual)) {
                problems.add("feasible starts differ"
                        + "\n  missing (oracle only): " + times(minus(expected, actual))
                        + "\n  extra   (calc only)  : " + times(minus(actual, expected)));
            }
            if (actual.size() != plans.size()) {
                problems.add("duplicate starts in output: " + plans.size() + " plans, "
                        + actual.size() + " distinct starts");
            }
            if (!plans.stream().map(VisitPlan::start).toList()
                    .equals(plans.stream().map(VisitPlan::start).sorted().toList())) {
                problems.add("plans are not sorted by start ascending");
            }
            problems.addAll(structuralProblems(inst, oracle, plans));

            if (!problems.isEmpty()) {
                failures.add("seed " + seed);
                if (worst == null || inst.complexity() < worst.complexity()) {
                    worst = inst;
                    worstReport = dump(inst, expected, actual, problems);
                }
            }
        }

        if (!failures.isEmpty()) {
            fail(failures.size() + " of " + seedCount + " seeds failed (memo=" + useMemo + "): "
                    + failures.subList(0, Math.min(25, failures.size()))
                    + "\n\nSMALLEST FAILING INSTANCE\n" + worstReport);
        }
    }

    private static List<String> structuralProblems(Instance inst, Oracle oracle,
                                                   List<VisitPlan> plans) {
        List<String> problems = new ArrayList<>();
        int earliest = ceilGrid(inst.notBefore());

        for (VisitPlan plan : plans) {
            String tag = "plan@" + hhmmss(secondsOf(plan.start())) + " bay=" + plan.bayId() + ": ";

            int bayIndex = -1;
            for (int i = 0; i < inst.bays().size(); i++) {
                if (inst.bays().get(i).bayId().equals(plan.bayId())) {
                    bayIndex = i;
                }
            }
            if (bayIndex < 0) {
                problems.add(tag + "unknown bay id");
                continue;
            }
            BaySchedule bay = inst.bays().get(bayIndex);
            if (!acceptsAll(inst.services(), bay)) {
                problems.add(tag + "bay type " + bay.bayTypeId() + " not accepted by every service");
            }

            List<SegmentPlan> segments = plan.segments();
            if (segments.size() != inst.services().size()) {
                problems.add(tag + "segment count " + segments.size() + " != service count "
                        + inst.services().size());
                continue;
            }
            List<Integer> planned = segments.stream().map(SegmentPlan::serviceId).sorted().toList();
            List<Integer> requested = inst.services().stream().map(Service::getId).sorted().toList();
            if (!planned.equals(requested)) {
                problems.add(tag + "segments cover " + planned + " but requested " + requested);
                continue;
            }
            if (secondsOf(plan.start()) < earliest) {
                problems.add(tag + "starts before ceilToGrid(notBefore)=" + hhmmss(earliest));
            }

            int previousEnd = -1;
            for (SegmentPlan segment : segments) {
                Service service = inst.services().stream()
                        .filter(s -> s.getId().equals(segment.serviceId())).findFirst().orElseThrow();
                int from = secondsOf(segment.time().lower());
                int to = secondsOf(segment.time().upper());
                if (from % GRID != 0) {
                    problems.add(tag + "segment " + service.getId() + " start " + hhmmss(from)
                            + " is off the 15-minute grid");
                }
                if (to - from != service.getDurationMinutes() * 60) {
                    problems.add(tag + "segment " + service.getId() + " length " + (to - from) / 60
                            + "min != duration " + service.getDurationMinutes() + "min");
                }
                if (previousEnd >= 0) {
                    int gap = from - previousEnd;
                    if (gap < 0 || gap > MAX_GAP) {
                        problems.add(tag + "gap of " + gap / 60 + "min before segment "
                                + service.getId() + " is outside [0,15]");
                    }
                }
                previousEnd = to;
                problems.addAll(assignmentProblems(inst, oracle, tag, service, segment, from, to));
            }

            int spanFrom = secondsOf(plan.start());
            int spanTo = secondsOf(plan.end());
            if (!coveredBy(oracle.bayFree.get(bayIndex), spanFrom, spanTo)) {
                problems.add(tag + "span " + hhmmss(spanFrom) + "-" + hhmmss(spanTo)
                        + " is not inside a single free range of the bay");
            }

            for (int i = 0; i < inst.bays().size(); i++) {
                if (inst.bays().get(i).bayId() < plan.bayId() && oracle.feasibleAtInBay(spanFrom, i)) {
                    problems.add(tag + "bay " + inst.bays().get(i).bayId()
                            + " is also feasible here but has a lower id (tie-break violated)");
                }
            }
        }
        return problems;
    }

    private static List<String> assignmentProblems(Instance inst, Oracle oracle, String tag,
                                                   Service service, SegmentPlan segment,
                                                   int from, int to) {
        List<String> problems = new ArrayList<>();

        Map<Integer, UserId> employees = segment.employeeByRequirementId();
        Set<Integer> employeeReqIds = service.getEmployeeRequirements().stream()
                .map(EmployeeRequirement::getId).collect(java.util.stream.Collectors.toSet());
        if (!employees.keySet().equals(employeeReqIds)) {
            problems.add(tag + "employee map keys " + employees.keySet() + " != requirement ids "
                    + employeeReqIds);
        }
        if (new HashSet<>(employees.values()).size() != employees.size()) {
            problems.add(tag + "same employee assigned to two requirements of service "
                    + service.getId());
        }
        for (EmployeeRequirement req : service.getEmployeeRequirements()) {
            UserId assigned = employees.get(req.getId());
            if (assigned == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < inst.employees().size(); i++) {
                if (inst.employees().get(i).employeeId().equals(assigned)) {
                    index = i;
                }
            }
            if (index < 0) {
                problems.add(tag + "employee " + assigned + " is not in the input");
                continue;
            }
            EmployeeSchedule schedule = inst.employees().get(index);
            if (Collections.disjoint(schedule.roleIds(), req.getRoleIds())) {
                problems.add(tag + "employee " + short_(assigned) + " roles " + schedule.roleIds()
                        + " do not satisfy requirement " + req.getId() + " " + req.getRoleIds());
            }
            if (!coveredBy(oracle.employeeFree.get(index), from, to)) {
                problems.add(tag + "employee " + short_(assigned) + " is not free for "
                        + hhmmss(from) + "-" + hhmmss(to));
            }
        }

        Map<Integer, Integer> equipment = segment.equipmentByRequirementId();
        Set<Integer> equipmentReqIds = service.getEquipmentRequirements().stream()
                .map(EquipmentRequirement::getId).collect(java.util.stream.Collectors.toSet());
        if (!equipment.keySet().equals(equipmentReqIds)) {
            problems.add(tag + "equipment map keys " + equipment.keySet() + " != requirement ids "
                    + equipmentReqIds);
        }
        if (new HashSet<>(equipment.values()).size() != equipment.size()) {
            problems.add(tag + "same equipment unit assigned to two requirements of service "
                    + service.getId());
        }
        for (EquipmentRequirement req : service.getEquipmentRequirements()) {
            Integer assigned = equipment.get(req.getId());
            if (assigned == null) {
                continue;
            }
            int index = -1;
            for (int i = 0; i < inst.equipment().size(); i++) {
                if (inst.equipment().get(i).equipmentId().equals(assigned)) {
                    index = i;
                }
            }
            if (index < 0) {
                problems.add(tag + "equipment " + assigned + " is not in the input");
                continue;
            }
            EquipmentSchedule schedule = inst.equipment().get(index);
            if (!req.getEquipmentTypeIds().contains(schedule.equipmentTypeId())) {
                problems.add(tag + "equipment " + assigned + " type " + schedule.equipmentTypeId()
                        + " does not satisfy requirement " + req.getId() + " "
                        + req.getEquipmentTypeIds());
            }
            if (!coveredBy(oracle.equipmentFree.get(index), from, to)) {
                problems.add(tag + "equipment " + assigned + " is not free for " + hhmmss(from)
                        + "-" + hhmmss(to));
            }
        }
        return problems;
    }

    // ------------------------------------------------------------------ reporting

    private static TreeSet<Integer> minus(Collection<Integer> a, Collection<Integer> b) {
        TreeSet<Integer> out = new TreeSet<>(a);
        out.removeAll(b);
        return out;
    }

    private static String times(Collection<Integer> seconds) {
        return seconds.stream().map(SlotCalculatorOracleTest::hhmmss).toList().toString();
    }

    private static String hhmmss(int seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    private static String short_(UserId id) {
        return "E" + id.id().getLeastSignificantBits();
    }

    private static String ranges(List<TimeRange> free) {
        return free.stream()
                .map(r -> hhmmss(secondsOf(r.lower())) + "-" + hhmmss(secondsOf(r.upper())))
                .toList().toString();
    }

    private static String dump(Instance inst, Collection<Integer> expected,
                               Collection<Integer> actual, List<String> problems) {
        StringBuilder sb = new StringBuilder();
        sb.append("seed = ").append(inst.seed()).append('\n');
        sb.append("notBefore = ").append(inst.notBefore()).append(" -> earliest grid start ")
                .append(hhmmss(ceilGrid(inst.notBefore()))).append('\n');
        sb.append("services:\n");
        for (Service s : inst.services()) {
            sb.append("  id=").append(s.getId()).append(" duration=").append(s.getDurationMinutes())
                    .append("min bayTypes=").append(new TreeSet<>(s.getServiceBayTypeIds()))
                    .append(" employeeReqs=")
                    .append(s.getEmployeeRequirements().stream()
                            .map(r -> r.getId() + new TreeSet<>(r.getRoleIds()).toString()).toList())
                    .append(" equipmentReqs=")
                    .append(s.getEquipmentRequirements().stream()
                            .map(r -> r.getId() + new TreeSet<>(r.getEquipmentTypeIds()).toString())
                            .toList())
                    .append('\n');
        }
        sb.append("bays:\n");
        for (BaySchedule b : inst.bays()) {
            sb.append("  id=").append(b.bayId()).append(" type=").append(b.bayTypeId())
                    .append(" free=").append(ranges(b.free())).append('\n');
        }
        sb.append("employees:\n");
        for (EmployeeSchedule e : inst.employees()) {
            sb.append("  ").append(short_(e.employeeId())).append(" roles=")
                    .append(new TreeSet<>(e.roleIds())).append(" free=").append(ranges(e.free()))
                    .append('\n');
        }
        sb.append("equipment:\n");
        for (EquipmentSchedule e : inst.equipment()) {
            sb.append("  id=").append(e.equipmentId()).append(" type=").append(e.equipmentTypeId())
                    .append(" free=").append(ranges(e.free())).append('\n');
        }
        sb.append("oracle starts    = ").append(times(expected)).append('\n');
        sb.append("calculator starts= ").append(times(actual)).append('\n');
        sb.append("problems:\n");
        problems.forEach(p -> sb.append("  - ").append(p).append('\n'));
        return sb.toString();
    }

    @Test
    void generated_corpus_is_not_degenerate() {
        int withPlans = 0;
        int totalStarts = 0;
        int multiSegment = 0;
        int withRealGap = 0;
        int offGridBayBoundary = 0;
        int twoBaysBothFeasible = 0;
        int equipmentUsed = 0;
        int notBeforeTrimmed = 0;

        for (int seed = 0; seed < SEEDS; seed++) {
            Instance inst = generate(seed);
            List<VisitPlan> plans = SlotCalculator.computeVisits(inst.services(), inst.bays(),
                    inst.employees(), inst.equipment(), inst.notBefore());
            if (!plans.isEmpty()) {
                withPlans++;
            }
            totalStarts += plans.size();
            if (inst.bays().stream().flatMap(b -> b.free().stream())
                    .anyMatch(r -> secondsOf(r.lower()) % GRID != 0)) {
                offGridBayBoundary++;
            }
            if (!inst.notBefore().equals(DAY) && !plans.isEmpty()) {
                notBeforeTrimmed++;
            }
            if (plans.stream().map(VisitPlan::bayId).distinct().count() > 1) {
                twoBaysBothFeasible++;
            }
            for (VisitPlan plan : plans) {
                if (plan.segments().size() > 1) {
                    multiSegment++;
                    for (int i = 1; i < plan.segments().size(); i++) {
                        if (!plan.segments().get(i).time().lower()
                                .equals(plan.segments().get(i - 1).time().upper())) {
                            withRealGap++;
                            break;
                        }
                    }
                }
                if (plan.segments().stream()
                        .anyMatch(s -> !s.equipmentByRequirementId().isEmpty())) {
                    equipmentUsed++;
                }
            }
        }

        System.out.println("corpus: seeds=" + SEEDS + " seedsWithPlans=" + withPlans
                + " totalStarts=" + totalStarts + " multiSegmentPlans=" + multiSegment
                + " plansWithNonZeroGap=" + withRealGap + " plansUsingEquipment=" + equipmentUsed
                + " seedsWithOffGridBayBoundary=" + offGridBayBoundary
                + " seedsWithNonZeroNotBeforeAndPlans=" + notBeforeTrimmed
                + " seedsWherePlansSpanTwoBays=" + twoBaysBothFeasible);

        assertThat(withPlans).as("seeds producing at least one plan").isGreaterThan(SEEDS / 10);
        assertThat(totalStarts).as("total feasible starts across the corpus").isGreaterThan(2000);
        assertThat(multiSegment).as("multi-segment plans").isGreaterThan(200);
        assertThat(withRealGap).as("plans with a non-zero inter-segment gap").isGreaterThan(50);
        assertThat(equipmentUsed).as("plans that assign equipment").isGreaterThan(200);
        assertThat(offGridBayBoundary).as("seeds with an off-grid bay boundary")
                .isGreaterThan(SEEDS / 4);
        assertThat(notBeforeTrimmed).as("seeds with a non-midnight notBefore that still yield plans")
                .isGreaterThan(50);
        assertThat(twoBaysBothFeasible).as("seeds whose plans come from more than one bay")
                .isGreaterThan(50);
    }

    // ------------------------------------------------------------------ determinism

    @Test
    void results_are_independent_of_input_list_order_and_repeated_runs() {
        for (int seed = 0; seed < 500; seed++) {
            Instance inst = generate(seed);
            List<VisitPlan> reference = SlotCalculator.computeVisits(inst.services(), inst.bays(),
                    inst.employees(), inst.equipment(), inst.notBefore());

            Random rnd = new Random(~seed);
            List<Service> services = new ArrayList<>(inst.services());
            List<BaySchedule> bays = new ArrayList<>(inst.bays());
            List<EmployeeSchedule> employees = new ArrayList<>(inst.employees());
            List<EquipmentSchedule> equipment = new ArrayList<>(inst.equipment());
            Collections.shuffle(services, rnd);
            Collections.shuffle(bays, rnd);
            Collections.shuffle(employees, rnd);
            Collections.shuffle(equipment, rnd);

            List<VisitPlan> shuffled = SlotCalculator.computeVisits(services, bays, employees,
                    equipment, inst.notBefore());

            assertThat(shuffled).as("seed %d: shuffled input changed the plans", seed)
                    .isEqualTo(reference);
            assertThat(SlotCalculator.computeVisits(inst.services(), inst.bays(), inst.employees(),
                    inst.equipment(), inst.notBefore()))
                    .as("seed %d: repeated run changed the plans", seed).isEqualTo(reference);
        }
    }

    @Test
    void starts_for_all_bays_equal_the_union_of_the_per_bay_starts() {
        for (int seed = 0; seed < 1500; seed++) {
            Instance inst = generate(seed);
            if (inst.bays().size() < 2) {
                continue;
            }
            TreeSet<Integer> combined = new TreeSet<>();
            SlotCalculator.computeVisits(inst.services(), inst.bays(), inst.employees(),
                    inst.equipment(), inst.notBefore()).forEach(p -> combined.add(secondsOf(p.start())));

            TreeSet<Integer> perBay = new TreeSet<>();
            for (BaySchedule bay : inst.bays()) {
                SlotCalculator.computeVisits(inst.services(), List.of(bay), inst.employees(),
                        inst.equipment(), inst.notBefore())
                        .forEach(p -> perBay.add(secondsOf(p.start())));
            }
            assertThat(combined).as("seed %d: cross-bay result is not the union of per-bay results",
                    seed).isEqualTo(perBay);
        }
    }

    // ------------------------------------------------------------------ ceilToGrid

    @Test
    void ceilToGrid_ignores_nothing_below_the_minute_and_always_lands_clean() {
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 10, 0)))
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 10, 0));
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 10, 0, 1)))
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 10, 15));
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 10, 0).plusNanos(1)))
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 10, 15));
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 10, 14, 59)))
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 10, 15));
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 10, 14, 59, 999999999)))
                .isEqualTo(LocalDateTime.of(2026, 8, 14, 10, 15));
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 23, 59, 59)))
                .isEqualTo(LocalDateTime.of(2026, 8, 15, 0, 0));

        for (int second = 0; second < 3600; second++) {
            LocalDateTime t = LocalDateTime.of(2026, 8, 14, 9, 0).plusSeconds(second);
            LocalDateTime ceiled = SlotCalculator.ceilToGrid(t);
            assertThat(ceiled).as("second %d", second).isEqualTo(atSeconds(ceilGrid(t)));
            assertThat(ceiled.getSecond()).isZero();
            assertThat(ceiled.getNano()).isZero();
            assertThat(ceiled.getMinute() % 15).isZero();
        }
    }

    // ------------------------------------------------------------------ TimeRange algebra

    @Test
    void time_range_algebra_matches_a_minute_set_brute_force() {
        for (int seed = 0; seed < ALGEBRA_SEEDS; seed++) {
            Random rnd = new Random(seed);
            List<TimeRange> base = randomMinuteRanges(rnd, 5);
            List<TimeRange> cuts = randomMinuteRanges(rnd, 4);

            List<TimeRange> unioned = TimeRanges.union(base);
            assertThat(bits(unioned)).as("seed %d union minutes", seed).isEqualTo(bits(base));
            assertNormalised(seed, "union", unioned);

            List<TimeRange> difference = TimeRanges.subtractAll(base, cuts);
            BitSet expected = bits(base);
            expected.andNot(bits(cuts));
            assertThat(bits(difference)).as("seed %d subtractAll minutes: base=%s cuts=%s", seed,
                    base, cuts).isEqualTo(expected);
            assertNormalised(seed, "subtractAll", difference);

            assertThat(bits(TimeRanges.free(base, cuts))).as("seed %d free minutes", seed)
                    .isEqualTo(expected);

            TimeRange left = randomMinuteRange(rnd);
            TimeRange right = randomMinuteRange(rnd);

            BitSet expectedSubtract = bits(List.of(left));
            expectedSubtract.andNot(bits(List.of(right)));
            List<TimeRange> subtracted = left.subtract(right);
            assertThat(bits(subtracted)).as("seed %d %s.subtract(%s)", seed, left, right)
                    .isEqualTo(expectedSubtract);
            assertNormalised(seed, "subtract", subtracted);

            BitSet expectedIntersect = bits(List.of(left));
            expectedIntersect.and(bits(List.of(right)));
            Optional<TimeRange> intersection = left.intersect(right);
            assertThat(intersection.isPresent()).as("seed %d %s.intersect(%s)", seed, left, right)
                    .isEqualTo(!expectedIntersect.isEmpty());
            if (intersection.isPresent()) {
                assertThat(bits(List.of(intersection.get())))
                        .as("seed %d intersect minutes", seed).isEqualTo(expectedIntersect);
            }

            assertThat(left.contains(right))
                    .as("seed %d %s.contains(%s)", seed, left, right)
                    .isEqualTo(containsAll(bits(List.of(left)), bits(List.of(right))));
            assertThat(left.overlaps(right)).as("seed %d %s.overlaps(%s)", seed, left, right)
                    .isEqualTo(!expectedIntersect.isEmpty());
        }
    }

    private static void assertNormalised(int seed, String op, List<TimeRange> ranges) {
        for (int i = 1; i < ranges.size(); i++) {
            assertThat(ranges.get(i - 1).upper())
                    .as("seed %d %s: piece %d must end strictly before piece %d starts (%s)",
                            seed, op, i - 1, i, ranges)
                    .isBefore(ranges.get(i).lower());
        }
    }

    private static boolean containsAll(BitSet outer, BitSet inner) {
        BitSet copy = (BitSet) inner.clone();
        copy.andNot(outer);
        return copy.isEmpty();
    }

    private static BitSet bits(List<TimeRange> ranges) {
        BitSet set = new BitSet();
        for (TimeRange r : ranges) {
            set.set((int) ChronoUnit.MINUTES.between(DAY, r.lower()),
                    (int) ChronoUnit.MINUTES.between(DAY, r.upper()));
        }
        return set;
    }

    private static List<TimeRange> randomMinuteRanges(Random rnd, int max) {
        List<TimeRange> ranges = new ArrayList<>();
        int count = rnd.nextInt(max + 1);
        for (int i = 0; i < count; i++) {
            ranges.add(randomMinuteRange(rnd));
        }
        return ranges;
    }

    private static TimeRange randomMinuteRange(Random rnd) {
        int a = rnd.nextInt(40);
        int b = rnd.nextInt(40);
        while (a == b) {
            b = rnd.nextInt(40);
        }
        return TimeRange.of(DAY.plusMinutes(Math.min(a, b)), DAY.plusMinutes(Math.max(a, b)));
    }

    // ------------------------------------------------------------------ matcher

    @Test
    void requirement_matcher_matches_brute_force_distinct_assignment() {
        for (int seed = 0; seed < MATCHER_SEEDS; seed++) {
            Random rnd = new Random(seed);
            int slots = rnd.nextInt(5);
            int pool = 1 + rnd.nextInt(6);
            List<List<Integer>> candidates = new ArrayList<>();
            for (int s = 0; s < slots; s++) {
                List<Integer> row = new ArrayList<>();
                for (int r = 0; r < pool; r++) {
                    if (rnd.nextInt(3) > 0) {
                        row.add(r);
                    }
                }
                if (!row.isEmpty() && rnd.nextInt(5) == 0) {
                    row.add(row.get(rnd.nextInt(row.size())));
                }
                candidates.add(row);
            }

            boolean expected = hasSystemOfDistinctRepresentatives(candidates);
            Optional<Map<Integer, Integer>> actual = RequirementMatcher.match(candidates);
            assertThat(actual.isPresent()).as("seed %d candidates %s", seed, candidates)
                    .isEqualTo(expected);

            if (actual.isPresent()) {
                Map<Integer, Integer> assignment = actual.get();
                assertThat(assignment.keySet()).as("seed %d slots covered", seed)
                        .containsExactlyInAnyOrderElementsOf(
                                java.util.stream.IntStream.range(0, slots).boxed().toList());
                assertThat(new HashSet<>(assignment.values()).size())
                        .as("seed %d assignment must be injective: %s", seed, assignment)
                        .isEqualTo(assignment.size());
                for (Map.Entry<Integer, Integer> entry : assignment.entrySet()) {
                    assertThat(candidates.get(entry.getKey()))
                            .as("seed %d slot %d got a non-candidate", seed, entry.getKey())
                            .contains(entry.getValue());
                }
                assertThat(RequirementMatcher.match(candidates))
                        .as("seed %d matcher is not deterministic", seed).isEqualTo(actual);
            }
        }
    }

    /** Reachable only in-domain: SlotService rejects duplicate serviceIds before this point. */
    @Test
    void the_same_service_booked_twice_becomes_two_sequential_segments() {
        //given
        Service oilChange = svc(1, 30, Set.of(10));

        //when
        List<VisitPlan> plans = SlotCalculator.computeVisits(List.of(oilChange, oilChange),
                List.of(new BaySchedule(100, 1, List.of(hours(9, 10)))), mechanicAllDay(),
                List.of(), DAY);

        //then
        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().segments()).extracting(SegmentPlan::serviceId)
                .containsExactly(1, 1);
        assertThat(plans.getFirst().segments()).extracting(SegmentPlan::time)
                .containsExactly(TimeRange.of(at(9, 0), at(9, 30)),
                        TimeRange.of(at(9, 30), at(10, 0)));
    }

    /**
     * Documents an accepted trade-off (spec section 6 step 4: "accept the first feasible one").
     * Order [1,2] forces a 10-minute grid gap and ends 11:00; order [2,1] would end 10:50.
     * The set of feasible starts is unaffected, but the booked visit blocks 10 extra minutes.
     */
    @Test
    void plan_is_the_first_feasible_permutation_not_the_one_with_the_shortest_span() {
        //given
        List<Service> services = List.of(svc(1, 50, Set.of(10)), svc(2, 60, Set.of(10)));

        //when
        List<VisitPlan> plans = SlotCalculator.computeVisits(services,
                List.of(new BaySchedule(100, 1, List.of(hours(9, 11)))), mechanicAllDay(),
                List.of(), DAY);

        //then
        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().segments()).extracting(SegmentPlan::serviceId)
                .containsExactly(1, 2);
        assertThat(plans.getFirst().end()).isEqualTo(at(11, 0));
        assertThat(plans.getFirst().segments().get(1).time().lower()).isEqualTo(at(10, 0));
    }

    @Test
    void after_an_on_grid_segment_end_only_the_next_two_grid_points_are_offered() {
        //given
        UserId anna = UserId.of(new UUID(0L, 1L));
        UserId jan = UserId.of(new UUID(0L, 2L));
        List<Service> services = List.of(svc(1, 60, Set.of(10)), svc(2, 30, Set.of(11)));

        //when / then — first segment ends 10:00, so 10:00 and 10:15 work but 10:30 does not
        for (int minute : new int[]{0, 15}) {
            assertThat(startsWithSecondEmployeeFreeFrom(services, anna, jan, minute))
                    .as("second employee free from 10:%02d", minute).containsExactly(at(9, 0));
        }
        assertThat(startsWithSecondEmployeeFreeFrom(services, anna, jan, 30))
                .as("second employee free from 10:30").isEmpty();
    }

    private static List<LocalDateTime> startsWithSecondEmployeeFreeFrom(
            List<Service> services, UserId anna, UserId jan, int minute) {
        return SlotCalculator.computeVisits(services,
                List.of(new BaySchedule(100, 1, List.of(hours(9, 13)))),
                List.of(new EmployeeSchedule(anna, Set.of(10), List.of(hours(9, 10))),
                        new EmployeeSchedule(jan, Set.of(11), List.of(
                                TimeRange.of(at(10, minute), at(11, minute))))),
                List.of(), DAY).stream().map(VisitPlan::start).toList();
    }

    /** SlotService passes {@code LocalDateTime.now(branchClock)}, which carries seconds. */
    @Test
    void notBefore_carrying_seconds_skips_the_grid_point_it_lands_on() {
        //given / when / then
        assertThat(SlotCalculator.computeVisits(List.of(svc(1, 30, Set.of(10))),
                List.of(new BaySchedule(100, 1, List.of(hours(10, 11)))), mechanicAllDay(),
                List.of(), at(10, 15).plusSeconds(1)))
                .extracting(VisitPlan::start).containsExactly(at(10, 30));
        assertThat(SlotCalculator.computeVisits(List.of(svc(1, 30, Set.of(10))),
                List.of(new BaySchedule(100, 1, List.of(hours(10, 11)))), mechanicAllDay(),
                List.of(), at(10, 15)))
                .extracting(VisitPlan::start).containsExactly(at(10, 15), at(10, 30));
    }

    /**
     * Documents that the employee tie-break uses {@link UUID#compareTo} (signed longs), which is
     * NOT the byte-wise order PostgreSQL uses for {@code uuid}. Deterministic, but a query that
     * pre-sorted candidates in SQL would pick the other employee.
     */
    @Test
    void employee_tie_break_uses_java_signed_uuid_order_not_postgres_byte_order() {
        //given
        UUID highBitSet = UUID.fromString("ffffffff-0000-0000-0000-000000000000");
        UUID lowest = UUID.fromString("00000000-0000-0000-0000-000000000001");
        assertThat(highBitSet.compareTo(lowest)).isNegative();

        //when
        List<VisitPlan> plans = SlotCalculator.computeVisits(List.of(svc(1, 60, Set.of(10))),
                List.of(new BaySchedule(100, 1, List.of(hours(9, 10)))),
                List.of(new EmployeeSchedule(UserId.of(lowest), Set.of(10), List.of(hours(8, 14))),
                        new EmployeeSchedule(UserId.of(highBitSet), Set.of(10),
                                List.of(hours(8, 14)))),
                List.of(), DAY);

        //then
        assertThat(plans.getFirst().segments().getFirst().employeeByRequirementId())
                .containsEntry(10, UserId.of(highBitSet));
    }

    /** A same-date availability row may legitimately end at next-day 00:00; no start may cross it. */
    @Test
    void window_ending_at_next_midnight_never_yields_a_next_day_start() {
        //given
        TimeRange lateShift = TimeRange.of(at(23, 0), LocalDateTime.of(2026, 8, 15, 0, 0));

        //when
        List<VisitPlan> plans = SlotCalculator.computeVisits(List.of(svc(1, 15, Set.of(10))),
                List.of(new BaySchedule(100, 1, List.of(lateShift))),
                List.of(new EmployeeSchedule(UserId.of(new UUID(0L, 1L)), Set.of(10),
                        List.of(lateShift))), List.of(), DAY);

        //then
        assertThat(plans).extracting(VisitPlan::start)
                .containsExactly(at(23, 0), at(23, 15), at(23, 30), at(23, 45));
        assertThat(plans.getLast().end()).isEqualTo(LocalDateTime.of(2026, 8, 15, 0, 0));
    }

    private static Service svc(int id, int minutes, Set<Integer> roles) {
        return Service.of(id, "S" + id, null, (short) minutes, BigDecimal.TEN, ServiceStatus.ACTIVE,
                BRANCH_ID, 1, Set.of(1),
                List.of(EmployeeRequirement.of(id * 10, "r" + id, roles)), List.of());
    }

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2026, 8, 14, hour, minute);
    }

    private static TimeRange hours(int fromHour, int toHour) {
        return TimeRange.of(at(fromHour, 0), at(toHour, 0));
    }

    private static List<EmployeeSchedule> mechanicAllDay() {
        return List.of(new EmployeeSchedule(UserId.of(new UUID(0L, 1L)), Set.of(10),
                List.of(hours(8, 14))));
    }
}
