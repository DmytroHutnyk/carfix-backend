package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.user.UserId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SlotCalculatorTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UserId ANNA =
            UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UserId JAN =
            UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final int LIFT = 1;
    private static final int PIT = 2;
    private static final int ALIGNMENT_RIG = 3;
    private static final int MECHANIC = 10;
    private static final int SENIOR = 11;
    private static final int JACK_TYPE = 20;

    private static LocalDateTime at(int hour, int minute) {
        return LocalDateTime.of(2026, 8, 14, hour, minute);
    }

    private static TimeRange between(int fromH, int fromM, int toH, int toM) {
        return TimeRange.of(at(fromH, fromM), at(toH, toM));
    }

    private static Service service(int id, int durationMinutes,
                                   List<EmployeeRequirement> employeeReqs,
                                   List<EquipmentRequirement> equipmentReqs) {
        return Service.of(id, "Service " + id, null, (short) durationMinutes,
                BigDecimal.TEN, ServiceStatus.ACTIVE, BRANCH_ID, 1,
                Set.of(LIFT), employeeReqs, equipmentReqs);
    }

    private static Service simpleService(int id, int durationMinutes) {
        return service(id, durationMinutes,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))),
                List.of());
    }

    private static Service mechanicService(int id, int durationMinutes, Set<Integer> bayTypeIds) {
        return Service.of(id, "Service " + id, null, (short) durationMinutes,
                BigDecimal.TEN, ServiceStatus.ACTIVE, BRANCH_ID, 1, bayTypeIds,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
    }

    private static BaySchedule bay(TimeRange... free) {
        return new BaySchedule(100, LIFT, List.of(free));
    }

    private static BaySchedule bayOfType(int bayTypeId, TimeRange... free) {
        return new BaySchedule(100, bayTypeId, List.of(free));
    }

    private static EmployeeSchedule employee(UserId id, Set<Integer> roles, TimeRange... free) {
        return new EmployeeSchedule(id, roles, List.of(free));
    }

    @Test
    void test_single_service_full_day_yields_grid_of_starts() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start)
                .containsExactly(at(9, 0), at(9, 15), at(9, 30), at(9, 45),
                        at(10, 0), at(10, 15), at(10, 30), at(10, 45), at(11, 0));
        assertThat(plans.getFirst().end()).isEqualTo(at(10, 0));
        assertThat(plans.getFirst().bayId()).isEqualTo(100);
        assertThat(plans.getFirst().segments().getFirst().employeeByRequirementId())
                .containsEntry(1, ANNA);
    }

    @Test
    void test_notBefore_clamps_and_rounds_up_to_grid() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(10, 7));
        assertThat(plans).extracting(VisitPlan::start)
                .containsExactly(at(10, 15), at(10, 30), at(10, 45), at(11, 0));
    }

    @Test
    void test_off_grid_availability_start_rounds_up() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 30)),
                List.of(bay(between(9, 7, 10, 30))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start)
                .containsExactly(at(9, 15), at(9, 30), at(9, 45), at(10, 0));
    }

    @Test
    void test_busy_employee_removes_slots_bay_alone_would_allow() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC),
                        between(9, 0, 10, 0), between(11, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start)
                .containsExactly(at(9, 0), at(11, 0));
    }

    @Test
    void test_duration_not_fitting_any_window_yields_empty() {
        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 240)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_unqualified_employee_yields_empty() {
        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(SENIOR + 100), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_greedy_trap_two_slots_seated_by_reassignment() {
        Service engineReplacement = service(1, 60,
                List.of(EmployeeRequirement.of(1, "Engine specialist", Set.of(SENIOR)),
                        EmployeeRequirement.of(2, "Assisting mechanic", Set.of(SENIOR, MECHANIC))),
                List.of());
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(engineReplacement),
                List.of(bay(between(9, 0, 10, 0))),
                List.of(employee(ANNA, Set.of(SENIOR), between(9, 0, 12, 0)),
                        employee(JAN, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().segments().getFirst().employeeByRequirementId())
                .containsEntry(1, ANNA).containsEntry(2, JAN);
    }

    @Test
    void test_two_slots_one_qualified_employee_yields_empty() {
        Service twoMechanics = service(1, 60,
                List.of(EmployeeRequirement.of(1, "First", Set.of(MECHANIC)),
                        EmployeeRequirement.of(2, "Second", Set.of(MECHANIC))),
                List.of());
        assertThat(SlotCalculator.computeVisits(
                List.of(twoMechanics),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_two_equipment_slots_same_type_need_two_distinct_units() {
        Service clutch = service(1, 60,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))),
                List.of(EquipmentRequirement.of(1, "Front jack", Set.of(JACK_TYPE)),
                        EquipmentRequirement.of(2, "Rear jack", Set.of(JACK_TYPE))));
        List<EmployeeSchedule> staff =
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)));

        assertThat(SlotCalculator.computeVisits(List.of(clutch),
                List.of(bay(between(9, 0, 10, 0))), staff,
                List.of(new EquipmentSchedule(500, JACK_TYPE, List.of(between(9, 0, 12, 0)))),
                at(0, 0))).isEmpty();

        List<VisitPlan> plans = SlotCalculator.computeVisits(List.of(clutch),
                List.of(bay(between(9, 0, 10, 0))), staff,
                List.of(new EquipmentSchedule(500, JACK_TYPE, List.of(between(9, 0, 12, 0))),
                        new EquipmentSchedule(501, JACK_TYPE, List.of(between(9, 0, 12, 0)))),
                at(0, 0));
        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().segments().getFirst().equipmentByRequirementId())
                .containsOnlyKeys(1, 2);
        assertThat(plans.getFirst().segments().getFirst().equipmentByRequirementId().values())
                .containsExactlyInAnyOrder(500, 501);
    }

    @Test
    void test_chain_inserts_grid_gap_when_second_employee_frees_later() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(service(1, 50,
                                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()),
                        service(2, 60,
                                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of())),
                List.of(bay(between(9, 0, 11, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 10, 0)),
                        employee(JAN, Set.of(SENIOR), between(9, 55, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start).containsExactly(at(9, 0));
        VisitPlan plan = plans.getFirst();
        assertThat(plan.segments().get(0).time()).isEqualTo(between(9, 0, 9, 50));
        assertThat(plan.segments().get(1).time()).isEqualTo(between(10, 0, 11, 0));
        assertThat(plan.end()).isEqualTo(at(11, 0));
    }

    @Test
    void test_chain_gap_over_15_minutes_rejects_candidate() {
        assertThat(SlotCalculator.computeVisits(
                List.of(service(1, 50,
                                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()),
                        service(2, 60,
                                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of())),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 10, 0)),
                        employee(JAN, Set.of(SENIOR), between(10, 30, 11, 30))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_permutation_found_when_only_reversed_order_fits() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(service(1, 60,
                                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()),
                        service(2, 60,
                                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of())),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(10, 0, 11, 0)),
                        employee(JAN, Set.of(SENIOR), between(9, 0, 10, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).hasSize(1);
        VisitPlan plan = plans.getFirst();
        assertThat(plan.segments().get(0).serviceId()).isEqualTo(2);
        assertThat(plan.segments().get(1).serviceId()).isEqualTo(1);
        assertThat(plan.start()).isEqualTo(at(9, 0));
        assertThat(plan.end()).isEqualTo(at(11, 0));
    }

    @Test
    void test_whole_visit_must_fit_one_bay_window_across_a_real_gap() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60), simpleService(2, 60)),
                List.of(bay(between(9, 0, 10, 0), between(10, 15, 12, 15))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 13, 0)),
                        employee(JAN, Set.of(MECHANIC), between(9, 0, 13, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start).containsExactly(at(10, 15));
    }

    @Test
    void test_min_span_fits_window_but_real_span_with_gap_does_not() {
        List<EmployeeSchedule> staff = List.of(
                employee(ANNA, Set.of(MECHANIC), between(9, 0, 13, 0)),
                employee(JAN, Set.of(MECHANIC), between(9, 0, 13, 0)));

        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 50), simpleService(2, 50)),
                List.of(bay(between(9, 0, 10, 45))), staff, List.of(), at(0, 0))).isEmpty();

        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 50), simpleService(2, 50)),
                List.of(bay(between(9, 0, 10, 50))), staff, List.of(), at(0, 0)))
                .extracting(VisitPlan::start).containsExactly(at(9, 0));
    }

    @Test
    void test_backtracking_finds_start_greedy_placement_would_drop() {
        int roleA = 30;
        int roleB = 31;
        int roleC = 32;
        UserId ec = UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        List<Service> services = List.of(
                service(1, 15, List.of(EmployeeRequirement.of(1, "A", Set.of(roleA))), List.of()),
                service(2, 15, List.of(EmployeeRequirement.of(2, "B", Set.of(roleB))), List.of()),
                service(3, 15, List.of(EmployeeRequirement.of(3, "C", Set.of(roleC))), List.of()));

        List<VisitPlan> plans = SlotCalculator.computeVisits(services,
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(roleA), between(9, 0, 12, 0)),
                        employee(JAN, Set.of(roleB), between(9, 0, 12, 0)),
                        employee(ec, Set.of(roleC), between(10, 0, 10, 15))),
                List.of(),
                at(0, 0));

        assertThat(plans).extracting(VisitPlan::start).contains(at(9, 0));
        assertThat(plans.getFirst().start()).isEqualTo(at(9, 0));
    }

    @Test
    void test_two_bays_free_at_the_same_time_yield_one_plan_from_the_lowest_bay() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(new BaySchedule(100, LIFT, List.of(between(9, 0, 10, 0))),
                        new BaySchedule(200, LIFT, List.of(between(9, 0, 10, 0)))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).hasSize(1);
        assertThat(plans.getFirst().start()).isEqualTo(at(9, 0));
        assertThat(plans.getFirst().bayId()).isEqualTo(100);
    }

    @Test
    void test_second_bay_rescues_starts_the_first_bay_cannot_serve() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(new BaySchedule(100, LIFT, List.of(between(10, 0, 12, 0))),
                        new BaySchedule(200, LIFT, List.of(between(9, 0, 12, 0)))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).hasSize(9);
        assertThat(plans.getFirst().start()).isEqualTo(at(9, 0));
        assertThat(plans.getFirst().bayId()).isEqualTo(200);
        assertThat(plans.stream().filter(p -> p.start().equals(at(10, 0))).findFirst())
                .get().extracting(VisitPlan::bayId).isEqualTo(100);
    }

    @Test
    void test_employee_choice_is_deterministic_by_id() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(JAN, Set.of(MECHANIC), between(9, 0, 12, 0)),
                        employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));
        assertThat(plans).isNotEmpty();
        assertThat(plans).allSatisfy(plan -> assertThat(
                plan.segments().getFirst().employeeByRequirementId()).containsEntry(1, ANNA));
    }

    @Test
    void test_empty_service_list_yields_no_plans() {
        assertThat(SlotCalculator.computeVisits(
                List.of(),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_ceilToGrid_rolls_over_midnight_and_is_identity_on_grid() {
        assertThat(SlotCalculator.ceilToGrid(LocalDateTime.of(2026, 8, 14, 23, 50)))
                .isEqualTo(LocalDateTime.of(2026, 8, 15, 0, 0));
        assertThat(SlotCalculator.ceilToGrid(at(10, 0))).isEqualTo(at(10, 0));
        assertThat(SlotCalculator.ceilToGrid(at(10, 0).plusNanos(1))).isEqualTo(at(10, 15));
    }

    @Test
    void test_bay_of_unacceptable_type_yields_empty() {
        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 60)),
                List.of(bayOfType(PIT, between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
    }

    @Test
    void test_bay_type_must_be_accepted_by_every_service_not_just_one() {
        List<Service> services = List.of(
                mechanicService(1, 60, Set.of(LIFT, PIT)),
                mechanicService(2, 60, Set.of(PIT, ALIGNMENT_RIG)));
        List<EmployeeSchedule> staff =
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)));

        assertThat(SlotCalculator.computeVisits(services,
                List.of(bayOfType(LIFT, between(9, 0, 12, 0))), staff,
                List.of(), at(0, 0))).isEmpty();

        assertThat(SlotCalculator.computeVisits(services,
                List.of(bayOfType(PIT, between(9, 0, 12, 0))), staff,
                List.of(), at(0, 0)))
                .extracting(VisitPlan::start)
                .containsExactly(at(9, 0), at(9, 15), at(9, 30), at(9, 45), at(10, 0));
    }

    @Test
    void test_service_order_is_canonical_regardless_of_input_order() {
        Service first = service(1, 50,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
        Service second = service(2, 60,
                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of());
        List<BaySchedule> bays = List.of(bay(between(9, 0, 12, 0)));
        List<EmployeeSchedule> staff = List.of(
                employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)),
                employee(JAN, Set.of(SENIOR), between(9, 0, 12, 0)));

        List<VisitPlan> ascending = SlotCalculator.computeVisits(
                List.of(first, second), bays, staff, List.of(), at(0, 0));
        List<VisitPlan> descending = SlotCalculator.computeVisits(
                List.of(second, first), bays, staff, List.of(), at(0, 0));

        assertThat(ascending).isNotEmpty();
        assertThat(descending).extracting(VisitPlan::start)
                .containsExactlyElementsOf(ascending.stream().map(VisitPlan::start).toList());
        assertThat(descending).extracting(VisitPlan::end)
                .containsExactlyElementsOf(ascending.stream().map(VisitPlan::end).toList());
        assertThat(ascending.getFirst().segments()).extracting(SegmentPlan::serviceId)
                .containsExactly(1, 2);
        assertThat(descending.getFirst().segments()).extracting(SegmentPlan::serviceId)
                .containsExactly(1, 2);
    }

    @Test
    void test_same_mechanic_serves_every_segment_of_a_chain() {
        Service first = service(1, 60,
                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of());
        Service second = service(2, 60,
                List.of(EmployeeRequirement.of(2, "Mechanic", Set.of(MECHANIC))), List.of());

        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(first, second),
                List.of(bay(between(9, 0, 12, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0));

        assertThat(plans).isNotEmpty();
        VisitPlan plan = plans.getFirst();
        assertThat(plan.segments()).hasSize(2);
        assertThat(plan.segments().get(0).employeeByRequirementId()).containsExactly(entry(1, ANNA));
        assertThat(plan.segments().get(1).employeeByRequirementId()).containsExactly(entry(2, ANNA));
    }

    @Test
    void test_chain_accepts_gap_of_exactly_15_minutes() {
        List<VisitPlan> plans = SlotCalculator.computeVisits(
                List.of(service(1, 60,
                                List.of(EmployeeRequirement.of(1, "Mechanic", Set.of(MECHANIC))), List.of()),
                        service(2, 60,
                                List.of(EmployeeRequirement.of(2, "Senior", Set.of(SENIOR))), List.of())),
                List.of(bay(between(9, 0, 11, 15))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 10, 0)),
                        employee(JAN, Set.of(SENIOR), between(10, 15, 11, 15))),
                List.of(),
                at(0, 0));
        assertThat(plans).extracting(VisitPlan::start).containsExactly(at(9, 0));
        VisitPlan plan = plans.getFirst();
        assertThat(plan.segments().get(0).time()).isEqualTo(between(9, 0, 10, 0));
        assertThat(plan.segments().get(1).time()).isEqualTo(between(10, 15, 11, 15));
        assertThat(plan.end()).isEqualTo(at(11, 15));
    }

    @Test
    void test_planVisit_matches_computeVisits_entry_at_that_start() {
        List<Service> chain = List.of(simpleService(1, 60), simpleService(2, 30));
        List<BaySchedule> bays = List.of(bay(between(9, 0, 12, 0)));
        List<EmployeeSchedule> employees = List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)));

        VisitPlan listed = SlotCalculator.computeVisits(chain, bays, employees, List.of(), at(0, 0)).stream()
                .filter(p -> p.start().equals(at(9, 30)))
                .findFirst().orElseThrow();
        Optional<VisitPlan> planned = SlotCalculator.planVisit(chain, bays, employees, List.of(), at(9, 30), at(0, 0));

        assertThat(planned).contains(listed);
        assertThat(planned.get().segments()).hasSize(2);
        assertThat(planned.get().end()).isEqualTo(at(11, 0));
    }

    @Test
    void test_planVisit_rejects_off_grid_past_and_unfitting_starts() {
        List<Service> chain = List.of(simpleService(1, 60));
        List<BaySchedule> bays = List.of(bay(between(9, 0, 12, 0)));
        List<EmployeeSchedule> employees = List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)));

        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(9, 20), at(0, 0))).isEmpty();
        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(9, 0), at(9, 1))).isEmpty();
        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(11, 15), at(0, 0))).isEmpty();
        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(8, 45), at(0, 0))).isEmpty();
        assertThat(SlotCalculator.planVisit(List.of(), bays, employees, List.of(), at(9, 0), at(0, 0))).isEmpty();
        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(11, 0), at(0, 0))).isPresent();
    }

    @Test
    void test_planVisit_picks_the_lowest_id_bay_that_fits() {
        List<Service> chain = List.of(simpleService(1, 60));
        BaySchedule lowerBay = new BaySchedule(100, LIFT, List.of(between(9, 0, 12, 0)));
        BaySchedule higherBay = new BaySchedule(200, LIFT, List.of(between(9, 0, 12, 0)));
        List<EmployeeSchedule> employees = List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)));

        Optional<VisitPlan> plan = SlotCalculator.planVisit(chain, List.of(higherBay, lowerBay), employees, List.of(),
                at(9, 0), at(0, 0));

        assertThat(plan).isPresent();
        assertThat(plan.get().bayId()).isEqualTo(100);
    }

    @Test
    void test_isOnGrid_accepts_only_exact_quarter_hours() {
        assertThat(SlotCalculator.isOnGrid(LocalTime.of(10, 0))).isTrue();
        assertThat(SlotCalculator.isOnGrid(LocalTime.of(10, 15))).isTrue();
        assertThat(SlotCalculator.isOnGrid(LocalTime.of(10, 20))).isFalse();
        assertThat(SlotCalculator.isOnGrid(LocalTime.of(10, 0, 30))).isFalse();
    }

    @Test
    void test_planVisit_needs_a_free_employee_for_the_whole_segment() {
        List<Service> chain = List.of(simpleService(1, 60));
        List<BaySchedule> bays = List.of(bay(between(9, 0, 12, 0)));
        List<EmployeeSchedule> employees = List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 9, 45)));

        assertThat(SlotCalculator.planVisit(chain, bays, employees, List.of(), at(9, 0), at(0, 0))).isEmpty();
    }
}
