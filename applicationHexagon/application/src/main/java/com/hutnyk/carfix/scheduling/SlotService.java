package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.exception.BranchNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.scheduling.SlotPortIn;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;
import com.hutnyk.carfix.in.scheduling.query.DaySlotsView;
import com.hutnyk.carfix.in.scheduling.query.SlotView;
import com.hutnyk.carfix.out.availability.AvailabilityPortOut;
import com.hutnyk.carfix.out.branch.BranchPortOut;
import com.hutnyk.carfix.out.service.ServicePortOut;
import com.hutnyk.carfix.scheduling.exception.InvalidSlotQueryException;
import com.hutnyk.carfix.service.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@ApplicationService
public class SlotService implements SlotPortIn {

    private static final int MAX_RANGE_DAYS = 7;
    private static final int MAX_HORIZON_YEARS = 1;

    private final BranchPortOut branchPortOut;
    private final BranchScheduleLoader scheduleLoader;
    private final Clock clock;

    public SlotService(BranchPortOut branchPortOut, ServicePortOut servicePortOut,
                       AvailabilityPortOut availabilityPortOut, Clock clock) {
        this.branchPortOut = branchPortOut;
        this.scheduleLoader = new BranchScheduleLoader(servicePortOut, availabilityPortOut);
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public BranchSlotsView getSlots(BranchSlotsQuery query) {
        validateShape(query);

        BranchId branchId = BranchId.of(query.branchId());
        ZoneId branchZone = branchPortOut.findActiveBranchZone(branchId)
                .orElseThrow(() -> new BranchNotFoundException(query.branchId()));
        Clock branchClock = clock.withZone(branchZone);
        LocalDate today = LocalDate.now(branchClock);
        if (query.from().isBefore(today)) {
            throw new InvalidSlotQueryException("from must not be in the past");
        }
        if (query.from().isAfter(today.plusYears(MAX_HORIZON_YEARS))) {
            throw new InvalidSlotQueryException("from must be within " + MAX_HORIZON_YEARS + " year of today");
        }

        List<Service> services = scheduleLoader.loadServices(query.serviceIds(), branchId);
        List<LocalDate> dates = query.from().datesUntil(query.to().plusDays(1)).toList();

        Set<Integer> commonBayTypes = BranchScheduleLoader.commonBayTypes(services);
        if (commonBayTypes.isEmpty()) {
            return new BranchSlotsView(branchZone.getId(), false, emptyDays(dates));
        }

        Map<LocalDate, List<TimeRange>> openByDate =
                scheduleLoader.openRangesByDate(branchId, query.from(), query.to());
        if (openByDate.isEmpty()) {
            return new BranchSlotsView(branchZone.getId(), true, emptyDays(dates));
        }

        BranchResources resources = scheduleLoader.loadResources(branchId, services, commonBayTypes);
        if (!resources.canServe()) {
            return new BranchSlotsView(branchZone.getId(), true, emptyDays(dates));
        }

        Map<LocalDate, DaySchedules> schedules =
                scheduleLoader.loadSchedules(resources, query.from(), query.to(), openByDate);
        LocalDateTime now = LocalDateTime.now(branchClock);
        List<DaySlotsView> days = new ArrayList<>();
        for (LocalDate date : dates) {
            DaySchedules day = schedules.get(date);
            if (day == null) {
                days.add(new DaySlotsView(date, List.of()));
                continue;
            }
            LocalDateTime notBefore = date.isEqual(today) ? now : date.atStartOfDay();
            List<VisitPlan> plans = SlotCalculator.computeVisits(
                    services, day.bays(), day.employees(), day.equipment(), notBefore);
            days.add(new DaySlotsView(date, plans.stream()
                    .map(p -> new SlotView(p.start().toLocalTime(), p.end().toLocalTime()))
                    .toList()));
        }
        return new BranchSlotsView(branchZone.getId(), true, List.copyOf(days));
    }

    private void validateShape(BranchSlotsQuery query) {
        List<Integer> ids = query.serviceIds();
        if (ids == null || ids.isEmpty()) {
            throw new InvalidSlotQueryException("at least one serviceId is required");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new InvalidSlotQueryException("serviceIds must not contain null");
        }
        if (ids.size() > SlotCalculator.MAX_SERVICES_PER_VISIT) {
            throw new InvalidSlotQueryException(
                    "at most " + SlotCalculator.MAX_SERVICES_PER_VISIT + " services per visit");
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new InvalidSlotQueryException("serviceIds must be distinct");
        }
        if (query.from() == null || query.to() == null) {
            throw new InvalidSlotQueryException("from and to are required");
        }
        if (query.from().isAfter(query.to())) {
            throw new InvalidSlotQueryException("from must not be after to");
        }
        if (ChronoUnit.DAYS.between(query.from(), query.to()) + 1 > MAX_RANGE_DAYS) {
            throw new InvalidSlotQueryException("date range must be at most " + MAX_RANGE_DAYS + " days");
        }
    }

    private static List<DaySlotsView> emptyDays(List<LocalDate> dates) {
        return dates.stream().map(d -> new DaySlotsView(d, List.<SlotView>of())).toList();
    }
}
