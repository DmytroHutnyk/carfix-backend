package com.hutnyk.carfix.search;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.employee.EmployeeAvailability;
import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.employee.EmployeeId;
import com.hutnyk.carfix.equipment.Equipment;
import com.hutnyk.carfix.equipment.EquipmentAvailability;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.in.scheduling.query.EmployeeCandidateView;
import com.hutnyk.carfix.in.search.query.AvailabilityWindow;
import com.hutnyk.carfix.in.search.query.AvailableStartView;
import com.hutnyk.carfix.in.search.query.MatchedServiceView;
import com.hutnyk.carfix.in.search.query.WorkshopResultView;
import com.hutnyk.carfix.openingHours.OpeningCalendar;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.OpeningHoursException;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.BaySchedule;
import com.hutnyk.carfix.scheduling.EmployeeSchedule;
import com.hutnyk.carfix.scheduling.EquipmentSchedule;
import com.hutnyk.carfix.scheduling.SlotCalculator;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.TimeRanges;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.service.Service;
import com.hutnyk.carfix.service.ServiceStatus;
import com.hutnyk.carfix.serviceBay.ServiceBay;
import com.hutnyk.carfix.serviceBay.ServiceBayAvailability;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Batch-loads candidate schedules, then keeps each branch's first feasible starts in range. */
public final class SearchAvailabilityFilter {

    public static final int MAX_STARTS_PER_BRANCH = 3;

    private final ServicePortOut servicePortOut;
    private final AvailabilityPortOut availabilityPortOut;
    private final Clock clock;

    public SearchAvailabilityFilter(ServicePortOut servicePortOut, AvailabilityPortOut availabilityPortOut, Clock clock) {
        this.servicePortOut = servicePortOut;
        this.availabilityPortOut = availabilityPortOut;
        this.clock = clock;
    }

    private record Job(Service service, List<ServiceBay> bays,
                       List<EmployeeCandidateView> employees, List<Equipment> equipment) {
    }

    private record Calendars(Map<LocalDate, Map<Integer, List<TimeRange>>> bayAvailability,
                             Map<LocalDate, Map<Integer, List<TimeRange>>> bayOccupancy,
                             Map<LocalDate, Map<EmployeeId, List<TimeRange>>> employeeAvailability,
                             Map<LocalDate, Map<EmployeeId, List<TimeRange>>> employeeOccupancy,
                             Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentAvailability,
                             Map<LocalDate, Map<Integer, List<TimeRange>>> equipmentOccupancy) {
    }

    // SearchService guarantees valid date and optional time bounds.
    public List<WorkshopResultView> filter(List<WorkshopResultView> candidates, AvailabilityWindow window) {
        if (candidates.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<Job>> jobsByBranch = loadJobs(candidates);
        Calendars calendars = loadCalendars(jobsByBranch.values().stream().flatMap(List::stream).toList(),
                window.from(), window.to());
        List<BranchId> branchIds = candidates.stream().map(c -> BranchId.of(c.branchId())).toList();
        Map<BranchId, List<OpeningHours>> hours = availabilityPortOut.loadOpeningHoursByBranch(branchIds);
        Map<BranchId, List<OpeningHoursException>> exceptions =
                availabilityPortOut.loadOpeningHoursExceptionsByBranch(branchIds, window.from(), window.to());

        List<WorkshopResultView> kept = new ArrayList<>();
        for (WorkshopResultView candidate : candidates) {
            BranchId branchId = BranchId.of(candidate.branchId());
            Map<LocalDate, List<TimeRange>> openByDate = OpeningCalendar.of(
                            hours.getOrDefault(branchId, List.of()),
                            exceptions.getOrDefault(branchId, List.of()))
                    .openRangesByDate(window.from(), window.to());
            List<AvailableStartView> starts = firstStarts(candidate,
                    jobsByBranch.getOrDefault(candidate.branchId(), List.of()), calendars, window, openByDate);
            if (!starts.isEmpty()) {
                kept.add(candidate.withNextAvailableStarts(starts));
            }
        }
        return List.copyOf(kept);
    }

    private Map<UUID, List<Job>> loadJobs(List<WorkshopResultView> candidates) {
        Map<Integer, Service> services = servicePortOut.loadByIds(candidates.stream()
                        .flatMap(c -> c.matchedServices().stream())
                        .map(MatchedServiceView::serviceId)
                        .distinct()
                        .toList()).stream()
                .filter(s -> s.getStatus() == ServiceStatus.ACTIVE)
                .collect(Collectors.toMap(Service::getId, Function.identity()));

        List<BranchId> branchIds = candidates.stream().map(c -> BranchId.of(c.branchId())).toList();
        Map<BranchId, List<ServiceBay>> bays = availabilityPortOut.loadActiveBaysByBranch(branchIds);
        Map<BranchId, List<EmployeeCandidateView>> employees = availabilityPortOut.loadActiveEmployeesByBranch(branchIds);
        Map<BranchId, List<Equipment>> equipment = availabilityPortOut.loadActiveEquipmentByBranch(branchIds);

        Map<UUID, List<Job>> jobs = new LinkedHashMap<>();
        for (WorkshopResultView candidate : candidates) {
            BranchId branchId = BranchId.of(candidate.branchId());
            for (MatchedServiceView matched : candidate.matchedServices()) {
                Service service = services.get(matched.serviceId());
                if (service == null) {
                    continue;
                }
                jobs.computeIfAbsent(candidate.branchId(), id -> new ArrayList<>()).add(new Job(
                        service,
                        bays.getOrDefault(branchId, List.of()).stream()
                                .filter(b -> service.getServiceBayTypeIds().contains(b.getServiceBayTypeId()))
                                .toList(),
                        employees.getOrDefault(branchId, List.of()).stream()
                                .filter(e -> qualifiesAnyEmployeeSlot(service, e))
                                .toList(),
                        equipment.getOrDefault(branchId, List.of()).stream()
                                .filter(e -> qualifiesAnyEquipmentSlot(service, e))
                                .toList()));
            }
        }
        return jobs;
    }

    private Calendars loadCalendars(List<Job> jobs, LocalDate from, LocalDate to) {
        List<Integer> bayIds = jobs.stream().flatMap(j -> j.bays().stream()).map(ServiceBay::getId).distinct().toList();
        List<EmployeeId> employeeIds = jobs.stream().flatMap(j -> j.employees().stream())
                .map(EmployeeCandidateView::employeeId).distinct().toList();
        List<Integer> equipmentIds = jobs.stream().flatMap(j -> j.equipment().stream())
                .map(Equipment::getId).distinct().toList();
        return new Calendars(
                byDateAndResource(availabilityPortOut.loadBayAvailability(bayIds, from, to),
                        ServiceBayAvailability::getDate, ServiceBayAvailability::getServiceBayId,
                        ServiceBayAvailability::getAvailableTime),
                byDateAndResource(availabilityPortOut.loadBayOccupancy(bayIds, from, to),
                        ServiceBayBooking::getDate, ServiceBayBooking::getServiceBayId,
                        ServiceBayBooking::getBookedTime),
                byDateAndResource(availabilityPortOut.loadEmployeeAvailability(employeeIds, from, to),
                        EmployeeAvailability::getDate, EmployeeAvailability::getEmployeeId,
                        EmployeeAvailability::getAvailableTime),
                byDateAndResource(availabilityPortOut.loadEmployeeOccupancy(employeeIds, from, to),
                        EmployeeBooking::getDate, EmployeeBooking::getEmployeeId,
                        EmployeeBooking::getBookedTime),
                byDateAndResource(availabilityPortOut.loadEquipmentAvailability(equipmentIds, from, to),
                        EquipmentAvailability::getDate, EquipmentAvailability::getEquipmentId,
                        EquipmentAvailability::getAvailableTime),
                byDateAndResource(availabilityPortOut.loadEquipmentOccupancy(equipmentIds, from, to),
                        EquipmentBooking::getDate, EquipmentBooking::getEquipmentId,
                        EquipmentBooking::getBookedTime));
    }

    private List<AvailableStartView> firstStarts(WorkshopResultView candidate, List<Job> jobs,
                                                 Calendars calendars, AvailabilityWindow window,
                                                 Map<LocalDate, List<TimeRange>> openByDate) {
        if (jobs.isEmpty()) {
            return List.of();
        }
        Clock branchClock = clock.withZone(ZoneId.of(candidate.tz()));
        LocalDate today = LocalDate.now(branchClock);
        LocalDateTime now = LocalDateTime.now(branchClock);
        LocalTime dayStart = window.timeFrom() != null ? window.timeFrom() : LocalTime.MIDNIGHT;

        SortedSet<LocalDateTime> starts = new TreeSet<>();
        for (LocalDate date = window.from(); !date.isAfter(window.to()); date = date.plusDays(1)) {
            if (date.isBefore(today)) {
                continue;
            }
            List<TimeRange> open = openByDate.get(date);
            if (open == null) {
                continue;
            }
            // Later days cannot produce earlier starts, so a full set after a whole day is final
            if (starts.size() >= MAX_STARTS_PER_BRANCH) {
                break;
            }
            TimeRange dayWindow = TimeRange.of(date.atTime(dayStart),
                    window.timeTo() != null ? date.atTime(window.timeTo()) : date.plusDays(1).atStartOfDay());
            LocalDateTime notBefore = date.isEqual(today) && now.isAfter(dayWindow.lower()) ? now : dayWindow.lower();
            for (Job job : jobs) {
                List<VisitPlan> plans = SlotCalculator.computeVisits(List.of(job.service()),
                        baySchedules(job, calendars, date, open), employeeSchedules(job, calendars, date, open),
                        equipmentSchedules(job, calendars, date, open), notBefore);
                plans.stream().map(VisitPlan::start).filter(dayWindow::contains).forEach(starts::add);
            }
        }
        return starts.stream()
                .limit(MAX_STARTS_PER_BRANCH)
                .map(s -> new AvailableStartView(s.toLocalDate(), s.toLocalTime()))
                .toList();
    }

    private static List<BaySchedule> baySchedules(Job job, Calendars c, LocalDate date, List<TimeRange> open) {
        return job.bays().stream()
                .map(b -> new BaySchedule(b.getId(), b.getServiceBayTypeId(),
                        freeOf(c.bayAvailability(), c.bayOccupancy(), date, b.getId(), open)))
                .filter(s -> !s.free().isEmpty())
                .toList();
    }

    private static List<EmployeeSchedule> employeeSchedules(Job job, Calendars c, LocalDate date, List<TimeRange> open) {
        return job.employees().stream()
                .map(e -> new EmployeeSchedule(e.employeeId(), e.roleIds(),
                        freeOf(c.employeeAvailability(), c.employeeOccupancy(), date, e.employeeId(), open)))
                .filter(s -> !s.free().isEmpty())
                .toList();
    }

    private static List<EquipmentSchedule> equipmentSchedules(Job job, Calendars c, LocalDate date, List<TimeRange> open) {
        return job.equipment().stream()
                .map(e -> new EquipmentSchedule(e.getId(), e.getEquipmentTypeId(),
                        freeOf(c.equipmentAvailability(), c.equipmentOccupancy(), date, e.getId(), open)))
                .filter(s -> !s.free().isEmpty())
                .toList();
    }

    private static boolean qualifiesAnyEmployeeSlot(Service service, EmployeeCandidateView e) {
        return service.getEmployeeRequirements().stream()
                .anyMatch(req -> !Collections.disjoint(e.roleIds(), req.getRoleIds()));
    }

    private static boolean qualifiesAnyEquipmentSlot(Service service, Equipment e) {
        return service.getEquipmentRequirements().stream()
                .anyMatch(req -> req.getEquipmentTypeIds().contains(e.getEquipmentTypeId()));
    }

    private static <T, K> Map<LocalDate, Map<K, List<TimeRange>>> byDateAndResource(
            List<T> rows, Function<T, LocalDate> date, Function<T, K> resource, Function<T, TimeRange> range) {
        return rows.stream().collect(Collectors.groupingBy(date,
                Collectors.groupingBy(resource, Collectors.mapping(range, Collectors.toList()))));
    }

    private static <K> List<TimeRange> freeOf(Map<LocalDate, Map<K, List<TimeRange>>> avail,
                                              Map<LocalDate, Map<K, List<TimeRange>>> occ,
                                              LocalDate date, K resourceId, List<TimeRange> open) {
        return TimeRanges.intersect(
                TimeRanges.free(
                        avail.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of()),
                        occ.getOrDefault(date, Map.of()).getOrDefault(resourceId, List.of())),
                open);
    }
}
