package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.user.UserId;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public final class SlotCalculator {

    private static final int GRID_MINUTES = 15;
    private static final int MAX_GAP_MINUTES = 15;

    private SlotCalculator() {
    }

    public static List<VisitPlan> computeVisits(
            List<Service> services,
            List<BaySchedule> bays,
            List<EmployeeSchedule> employees,
            List<EquipmentSchedule> equipment,
            LocalDateTime notBefore) {

        List<BaySchedule> sortedBays = bays.stream()
                .sorted(Comparator.comparing(BaySchedule::bayId)).toList();
        List<EmployeeSchedule> sortedEmployees = employees.stream()
                .sorted(Comparator.comparing(s -> s.employeeId().id())).toList();
        List<EquipmentSchedule> sortedEquipment = equipment.stream()
                .sorted(Comparator.comparing(EquipmentSchedule::equipmentId)).toList();

        int minSpanMinutes = services.stream().mapToInt(Service::getDurationMinutes).sum();
        List<List<Service>> orders = permutations(services);

        SortedMap<LocalDateTime, VisitPlan> byStart = new TreeMap<>();
        for (BaySchedule bay : sortedBays) {
            for (TimeRange window : bay.free()) {
                LocalDateTime start = ceilToGrid(max(window.lower(), notBefore));
                while (!start.plusMinutes(minSpanMinutes).isAfter(window.upper())) {
                    if (!byStart.containsKey(start)) {
                        findPlan(orders, bay, window, start, sortedEmployees, sortedEquipment)
                                .ifPresent(plan -> byStart.put(plan.start(), plan));
                    }
                    start = start.plusMinutes(GRID_MINUTES);
                }
            }
        }
        return List.copyOf(byStart.values());
    }

    private static Optional<VisitPlan> findPlan(List<List<Service>> orders, BaySchedule bay,
                                                TimeRange window, LocalDateTime visitStart,
                                                List<EmployeeSchedule> employees,
                                                List<EquipmentSchedule> equipment) {
        for (List<Service> order : orders) {
            Optional<VisitPlan> plan = tryOrder(order, bay, window, visitStart, employees, equipment);
            if (plan.isPresent()) {
                return plan;
            }
        }
        return Optional.empty();
    }

    private static Optional<VisitPlan> tryOrder(List<Service> order, BaySchedule bay,
                                                TimeRange window, LocalDateTime visitStart,
                                                List<EmployeeSchedule> employees,
                                                List<EquipmentSchedule> equipment) {
        List<SegmentPlan> segments = new ArrayList<>();
        LocalDateTime prevEnd = visitStart;
        boolean first = true;
        for (Service service : order) {
            List<LocalDateTime> starts = first ? List.of(visitStart) : gridPointsWithin(prevEnd);
            first = false;
            SegmentPlan placed = null;
            for (LocalDateTime segmentStart : starts) {
                TimeRange segmentTime = TimeRange.of(
                        segmentStart, segmentStart.plusMinutes(service.getDurationMinutes()));
                if (!window.contains(segmentTime)) {
                    continue;
                }
                Optional<SegmentPlan> segment = trySegment(service, segmentTime, employees, equipment);
                if (segment.isPresent()) {
                    placed = segment.get();
                    break;
                }
            }
            if (placed == null) {
                return Optional.empty();
            }
            segments.add(placed);
            prevEnd = placed.time().upper();
        }
        return Optional.of(new VisitPlan(bay.bayId(), segments));
    }

    private static Optional<SegmentPlan> trySegment(Service service, TimeRange segmentTime,
                                                    List<EmployeeSchedule> employees,
                                                    List<EquipmentSchedule> equipment) {
        List<List<UserId>> employeeCandidates = service.getEmployeeRequirements().stream()
                .map(req -> employees.stream()
                        .filter(e -> !Collections.disjoint(e.roleIds(), req.getRoleIds()))
                        .filter(e -> covers(e.free(), segmentTime))
                        .map(EmployeeSchedule::employeeId)
                        .toList())
                .toList();
        Optional<Map<Integer, UserId>> employeeMatch = RequirementMatcher.match(employeeCandidates);
        if (employeeMatch.isEmpty()) {
            return Optional.empty();
        }

        List<List<Integer>> equipmentCandidates = service.getEquipmentRequirements().stream()
                .map(req -> equipment.stream()
                        .filter(e -> req.getEquipmentTypeIds().contains(e.equipmentTypeId()))
                        .filter(e -> covers(e.free(), segmentTime))
                        .map(EquipmentSchedule::equipmentId)
                        .toList())
                .toList();
        Optional<Map<Integer, Integer>> equipmentMatch = RequirementMatcher.match(equipmentCandidates);
        if (equipmentMatch.isEmpty()) {
            return Optional.empty();
        }

        Map<Integer, UserId> employeeByRequirementId = new LinkedHashMap<>();
        employeeMatch.get().forEach((slot, employeeId) -> employeeByRequirementId
                .put(service.getEmployeeRequirements().get(slot).getId(), employeeId));
        Map<Integer, Integer> equipmentByRequirementId = new LinkedHashMap<>();
        equipmentMatch.get().forEach((slot, equipmentId) -> equipmentByRequirementId
                .put(service.getEquipmentRequirements().get(slot).getId(), equipmentId));

        return Optional.of(new SegmentPlan(
                service.getId(), segmentTime, employeeByRequirementId, equipmentByRequirementId));
    }

    private static boolean covers(List<TimeRange> free, TimeRange target) {
        return free.stream().anyMatch(f -> f.contains(target));
    }

    private static List<LocalDateTime> gridPointsWithin(LocalDateTime prevEnd) {
        LocalDateTime limit = prevEnd.plusMinutes(MAX_GAP_MINUTES);
        List<LocalDateTime> points = new ArrayList<>(2);
        for (LocalDateTime p = ceilToGrid(prevEnd); !p.isAfter(limit); p = p.plusMinutes(GRID_MINUTES)) {
            points.add(p);
        }
        return points;
    }

    static LocalDateTime ceilToGrid(LocalDateTime t) {
        LocalDateTime floor = t.truncatedTo(ChronoUnit.HOURS)
                .plusMinutes((long) (t.getMinute() / GRID_MINUTES) * GRID_MINUTES);
        return floor.equals(t) ? t : floor.plusMinutes(GRID_MINUTES);
    }

    private static LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a : b;
    }

    private static List<List<Service>> permutations(List<Service> services) {
        if (services.size() <= 1) {
            return List.of(List.copyOf(services));
        }
        List<List<Service>> result = new ArrayList<>();
        permute(new ArrayList<>(services), 0, result);
        return result;
    }

    private static void permute(List<Service> current, int index, List<List<Service>> out) {
        if (index == current.size() - 1) {
            out.add(List.copyOf(current));
            return;
        }
        for (int i = index; i < current.size(); i++) {
            Collections.swap(current, index, i);
            permute(current, index + 1, out);
            Collections.swap(current, index, i);
        }
    }
}
