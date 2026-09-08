package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** Randomized proof that slot listing and booking recomputation produce identical feasible plans. */
class SlotCalculatorPlanVisitEquivalenceTest {

    private static final int SEEDS = 20_000;
    private static final LocalDate DAY = LocalDate.of(2026, 8, 14);

    /** Free ranges live inside 08:00-14:00; the scan covers every grid point of that window. */
    private static final int WINDOW_START_MINUTE = 8 * 60;
    private static final int WINDOW_END_MINUTE = 14 * 60;

    private static final int[] DURATIONS = {15, 30, 40, 45, 50, 60, 90};
    private static final List<Integer> BAY_TYPES = List.of(1, 2);
    private static final List<Integer> ROLES = List.of(10, 11, 12);
    private static final List<Integer> EQUIPMENT_TYPES = List.of(20, 21);

    private record Instance(List<Service> services,
                            List<BaySchedule> bays,
                            List<EmployeeSchedule> employees,
                            List<EquipmentSchedule> equipment,
                            LocalDateTime notBefore) {
    }

    @Test
    void test_planVisit_is_equivalent_to_computeVisits_on_every_grid_start() {
        int feasibleSeeds = 0;
        int totalPlans = 0;
        int multiSegmentPlans = 0;
        int plansWithAGap = 0;
        int plansOnASecondBay = 0;
        int plansUsingEquipment = 0;

        for (int seed = 0; seed < SEEDS; seed++) {
            Instance instance = randomInstance(new Random(seed));

            List<VisitPlan> listed = SlotCalculator.computeVisits(
                    instance.services(), instance.bays(), instance.employees(),
                    instance.equipment(), instance.notBefore());

            Map<LocalDateTime, VisitPlan> byStart = new LinkedHashMap<>();
            for (VisitPlan plan : listed) {
                assertThat(byStart.put(plan.start(), plan))
                        .withFailMessage("seed=%d: computeVisits returned two plans for start %s%n%s",
                                seed, plan.start(), describe(instance))
                        .isNull();
            }

            totalPlans += listed.size();
            feasibleSeeds += listed.isEmpty() ? 0 : 1;
            for (VisitPlan plan : listed) {
                multiSegmentPlans += plan.segments().size() > 1 ? 1 : 0;
                plansOnASecondBay += plan.bayId() > 100 ? 1 : 0;
                plansUsingEquipment += plan.segments().stream()
                        .anyMatch(s -> !s.equipmentByRequirementId().isEmpty()) ? 1 : 0;
                for (int i = 1; i < plan.segments().size(); i++) {
                    if (!plan.segments().get(i).time().lower()
                            .equals(plan.segments().get(i - 1).time().upper())) {
                        plansWithAGap++;
                        break;
                    }
                }
            }

            // Every listed start must be inside the scanned grid, otherwise the scan below could
            // silently miss a disagreement.
            assertThat(byStart.keySet())
                    .withFailMessage("seed=%d: computeVisits produced a start outside the scanned "
                            + "grid window %s..%s: %s%n%s",
                            seed, gridPoint(WINDOW_START_MINUTE), gridPoint(WINDOW_END_MINUTE),
                            byStart.keySet(), describe(instance))
                    .allSatisfy(start -> assertThat(scannedStarts()).contains(start));

            for (LocalDateTime start : scannedStarts()) {
                Optional<VisitPlan> planned = SlotCalculator.planVisit(
                        instance.services(), instance.bays(), instance.employees(),
                        instance.equipment(), start, instance.notBefore());
                VisitPlan expected = byStart.get(start);

                assertThat(planned.isPresent())
                        .withFailMessage("seed=%d start=%s: planVisit=%s but computeVisits=%s%n%s",
                                seed, start, planned.isPresent() ? "present" : "empty",
                                expected == null ? "absent" : "present", describe(instance))
                        .isEqualTo(expected != null);

                if (expected != null) {
                    assertThat(planned.orElseThrow())
                            .withFailMessage("seed=%d start=%s: plans differ%n  listed  = %s%n"
                                    + "  planned = %s%n%s",
                                    seed, start, render(expected), render(planned.orElseThrow()),
                                    describe(instance))
                            .isEqualTo(expected);
                }

                // A start earlier than notBefore is never plannable, whatever the schedules say.
                if (start.isBefore(instance.notBefore())) {
                    assertThat(planned)
                            .withFailMessage("seed=%d start=%s: planVisit accepted a start before "
                                    + "notBefore=%s%n%s",
                                    seed, start, instance.notBefore(), describe(instance))
                            .isEmpty();
                }
            }
        }

        // The property is only worth anything if the corpus actually contains bookable visits of
        // every interesting shape. Without this, an all-infeasible generator would "pass".
        System.out.printf("equivalence corpus: seeds=%d feasibleSeeds=%d plans=%d multiSegment=%d "
                        + "withGap=%d onSecondBay=%d withEquipment=%d%n",
                SEEDS, feasibleSeeds, totalPlans, multiSegmentPlans, plansWithAGap,
                plansOnASecondBay, plansUsingEquipment);
        assertThat(feasibleSeeds).as("seeds yielding at least one bookable visit").isGreaterThan(2_000);
        assertThat(totalPlans).as("bookable visits across the corpus").isGreaterThan(15_000);
        assertThat(multiSegmentPlans).as("multi-service chains").isGreaterThan(1_000);
        assertThat(plansWithAGap).as("chains needing a grid gap between segments").isGreaterThan(500);
        assertThat(plansOnASecondBay).as("visits served by the non-first bay").isGreaterThan(2_000);
        assertThat(plansUsingEquipment).as("visits reserving equipment").isGreaterThan(1_000);
    }

    @Test
    void test_planVisit_rejects_every_off_grid_start() {
        for (int seed = 0; seed < SEEDS; seed++) {
            Random random = new Random(seed);
            Instance instance = randomInstance(random);

            for (int offGridMinute : new int[] {1, 5, 7, 14, 16, 29}) {
                LocalDateTime start = gridPoint(WINDOW_START_MINUTE + offGridMinute);
                assertOffGridRejected(seed, instance, start);
            }
            // A grid minute spoiled by seconds / nanos is off-grid too.
            LocalDateTime onGrid = gridPoint(WINDOW_START_MINUTE + 15 * random.nextInt(1, 20));
            assertOffGridRejected(seed, instance, onGrid.plusSeconds(1));
            assertOffGridRejected(seed, instance, onGrid.plusNanos(1));
        }
    }

    private static void assertOffGridRejected(int seed, Instance instance, LocalDateTime start) {
        Optional<VisitPlan> planned = SlotCalculator.planVisit(
                instance.services(), instance.bays(), instance.employees(),
                instance.equipment(), start, instance.notBefore());
        assertThat(planned)
                .withFailMessage("seed=%d: planVisit accepted off-grid start %s%n%s",
                        seed, start, describe(instance))
                .isEmpty();
    }


    private static Instance randomInstance(Random random) {
        int serviceCount = random.nextInt(1, 4);
        List<Service> services = new ArrayList<>();
        int requirementId = 1;
        for (int i = 0; i < serviceCount; i++) {
            int employeeRequirements = random.nextInt(1, 3);
            List<EmployeeRequirement> employeeReqs = new ArrayList<>();
            for (int r = 0; r < employeeRequirements; r++) {
                employeeReqs.add(EmployeeRequirement.of(
                        requirementId++, "emp-req", nonEmptySubset(random, ROLES)));
            }
            int equipmentRequirements = random.nextInt(0, 3);
            List<EquipmentRequirement> equipmentReqs = new ArrayList<>();
            for (int r = 0; r < equipmentRequirements; r++) {
                equipmentReqs.add(EquipmentRequirement.of(
                        requirementId++, "eq-req", nonEmptySubset(random, EQUIPMENT_TYPES)));
            }
            services.add(Service.of(
                    i + 1, "Service " + (i + 1), null,
                    (short) DURATIONS[random.nextInt(DURATIONS.length)],
                    BigDecimal.TEN, ServiceStatus.ACTIVE, BranchId.genId(), 1,
                    nonEmptySubset(random, BAY_TYPES), employeeReqs, equipmentReqs));
        }

        // Bias types toward demand; uniform draws make ~96% of cases infeasible and the property vacuous.
        List<Integer> demandedBayTypes = new ArrayList<>(commonBayTypes(services));
        List<Integer> demandedRoles = services.stream()
                .flatMap(s -> s.getEmployeeRequirements().stream())
                .flatMap(r -> r.getRoleIds().stream()).distinct().sorted().toList();
        List<Integer> demandedEquipmentTypes = services.stream()
                .flatMap(s -> s.getEquipmentRequirements().stream())
                .flatMap(r -> r.getEquipmentTypeIds().stream()).distinct().sorted().toList();
        int equipmentDemand = services.stream()
                .mapToInt(s -> s.getEquipmentRequirements().size()).max().orElse(0);

        List<BaySchedule> bays = new ArrayList<>();
        int bayCount = random.nextInt(1, 3);
        for (int i = 0; i < bayCount; i++) {
            bays.add(new BaySchedule(100 * (i + 1),
                    biasedDraw(random, demandedBayTypes, BAY_TYPES), randomFree(random)));
        }

        List<EmployeeSchedule> employees = new ArrayList<>();
        int employeeCount = random.nextInt(1, 5);
        for (int i = 0; i < employeeCount; i++) {
            Set<Integer> roles = random.nextInt(10) < 7 && !demandedRoles.isEmpty()
                    ? nonEmptySubset(random, demandedRoles)
                    : nonEmptySubset(random, ROLES);
            employees.add(new EmployeeSchedule(employeeId(i + 1), roles, randomFree(random)));
        }

        List<EquipmentSchedule> equipment = new ArrayList<>();
        int equipmentCount = random.nextInt(equipmentDemand == 0 ? 0 : 1, 4);
        for (int i = 0; i < equipmentCount; i++) {
            equipment.add(new EquipmentSchedule(500 + i,
                    biasedDraw(random, demandedEquipmentTypes, EQUIPMENT_TYPES), randomFree(random)));
        }

        // Mostly midnight (a future date, nothing trimmed), sometimes a real "now" that clips the
        // morning — including off-grid values, which is where ceilToGrid(max(...)) earns its keep.
        LocalDateTime notBefore = random.nextInt(10) < 8
                ? DAY.atStartOfDay()
                : gridPoint(random.nextInt(7 * 60, 15 * 60));

        return new Instance(services, bays, employees, equipment, notBefore);
    }

    /** 0-3 ranges inside 08:00-14:00, half snapped to the grid and half deliberately not. */
    private static List<TimeRange> randomFree(Random random) {
        int count = random.nextInt(10) == 0 ? 0 : random.nextInt(1, 4);
        int span = WINDOW_END_MINUTE - WINDOW_START_MINUTE;
        List<TimeRange> ranges = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int from = random.nextInt(0, span);
            // Half the ranges run out to (near) the end of the window so that 90-minute chains have
            // somewhere to fit; the other half are arbitrary and usually too short.
            int to = random.nextBoolean()
                    ? span - random.nextInt(0, Math.min(90, span - from))
                    : from + random.nextInt(1, span - from + 1);
            if (random.nextBoolean()) {
                from -= from % 15;
                to = Math.min(span, to + (15 - to % 15) % 15);
            }
            ranges.add(TimeRange.of(
                    gridPoint(WINDOW_START_MINUTE + from), gridPoint(WINDOW_START_MINUTE + to)));
        }
        return ranges;
    }

    /** Draws from {@code demanded} 70% of the time, from the full pool otherwise. */
    private static Integer biasedDraw(Random random, List<Integer> demanded, List<Integer> pool) {
        return random.nextInt(10) < 7 && !demanded.isEmpty()
                ? oneOf(random, demanded)
                : oneOf(random, pool);
    }

    private static Set<Integer> commonBayTypes(List<Service> services) {
        Set<Integer> common = new TreeSet<>(services.getFirst().getServiceBayTypeIds());
        services.forEach(s -> common.retainAll(s.getServiceBayTypeIds()));
        return common;
    }

    private static Set<Integer> nonEmptySubset(Random random, List<Integer> pool) {
        Set<Integer> chosen = new TreeSet<>();
        for (Integer candidate : pool) {
            if (random.nextBoolean()) {
                chosen.add(candidate);
            }
        }
        if (chosen.isEmpty()) {
            chosen.add(oneOf(random, pool));
        }
        return chosen;
    }

    private static Integer oneOf(Random random, List<Integer> pool) {
        return pool.get(random.nextInt(pool.size()));
    }

    private static EmployeeId employeeId(int n) {
        return EmployeeId.of(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }

    private static LocalDateTime gridPoint(int minuteOfDay) {
        return DAY.atStartOfDay().plusMinutes(minuteOfDay);
    }

    private static List<LocalDateTime> scannedStarts() {
        List<LocalDateTime> starts = new ArrayList<>();
        for (int m = WINDOW_START_MINUTE; m <= WINDOW_END_MINUTE; m += 15) {
            starts.add(gridPoint(m));
        }
        return starts;
    }


    private static String render(VisitPlan plan) {
        return "bay=" + plan.bayId() + " " + plan.segments().stream()
                .map(s -> "svc%d@[%s,%s) emp=%s eq=%s".formatted(
                        s.serviceId(), s.time().lower().toLocalTime(), s.time().upper().toLocalTime(),
                        s.employeeByRequirementId().entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, e -> shortId(e.getValue()))),
                        s.equipmentByRequirementId()))
                .collect(Collectors.joining(" | "));
    }

    private static String shortId(EmployeeId id) {
        String text = id.id().toString();
        return text.substring(text.length() - 3);
    }

    private static String describe(Instance instance) {
        StringBuilder text = new StringBuilder("  notBefore=").append(instance.notBefore());
        for (Service service : instance.services()) {
            text.append("\n  service ").append(service.getId())
                    .append(" dur=").append(service.getDurationMinutes())
                    .append(" bayTypes=").append(new TreeSet<>(service.getServiceBayTypeIds()))
                    .append(" empReqs=").append(service.getEmployeeRequirements().stream()
                            .map(r -> r.getId() + ":" + new TreeSet<>(r.getRoleIds())).toList())
                    .append(" eqReqs=").append(service.getEquipmentRequirements().stream()
                            .map(r -> r.getId() + ":" + new TreeSet<>(r.getEquipmentTypeIds())).toList());
        }
        for (BaySchedule bay : instance.bays()) {
            text.append("\n  bay ").append(bay.bayId()).append(" type=").append(bay.bayTypeId())
                    .append(" free=").append(renderFree(bay.free()));
        }
        for (EmployeeSchedule employee : instance.employees()) {
            text.append("\n  employee ").append(shortId(employee.employeeId()))
                    .append(" roles=").append(new TreeSet<>(employee.roleIds()))
                    .append(" free=").append(renderFree(employee.free()));
        }
        for (EquipmentSchedule unit : instance.equipment()) {
            text.append("\n  equipment ").append(unit.equipmentId())
                    .append(" type=").append(unit.equipmentTypeId())
                    .append(" free=").append(renderFree(unit.free()));
        }
        return text.toString();
    }

    private static String renderFree(List<TimeRange> free) {
        return free.stream()
                .map(r -> "[%s,%s)".formatted(r.lower().toLocalTime(), r.upper().toLocalTime()))
                .collect(Collectors.joining(","));
    }
}
