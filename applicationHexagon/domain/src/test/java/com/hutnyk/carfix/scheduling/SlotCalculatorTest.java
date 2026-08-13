package com.hutnyk.carfix.scheduling;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.service.EmployeeRequirement;
import com.hutnyk.carfix.service.EquipmentRequirement;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.user.UserId;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class SlotCalculatorTest {

    private static final BranchId BRANCH_ID = BranchId.genId();
    private static final UserId ANNA = UserId.genId();
    private static final UserId JAN = UserId.genId();
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
    void test_whole_visit_must_fit_one_bay_window() {
        assertThat(SlotCalculator.computeVisits(
                List.of(simpleService(1, 60), simpleService(2, 60)),
                List.of(bay(between(9, 0, 10, 0), between(10, 0, 11, 0))),
                List.of(employee(ANNA, Set.of(MECHANIC), between(9, 0, 12, 0)),
                        employee(JAN, Set.of(MECHANIC), between(9, 0, 12, 0))),
                List.of(),
                at(0, 0))).isEmpty();
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
}
