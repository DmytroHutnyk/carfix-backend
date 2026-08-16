package com.hutnyk.carfix.branch.adapter;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.repository.BookingRepository;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.branch.mapper.BranchMapper;
import com.hutnyk.carfix.branch.repository.BranchRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.employee.EmployeeStatus;
import com.hutnyk.carfix.employee.repository.EmployeeAvailabilityRepository;
import com.hutnyk.carfix.employee.repository.EmployeeRepository;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.openingHours.OpeningSchedule;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;
import com.hutnyk.carfix.openingHours.mapper.OpeningHoursMapper;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursExceptionRepository;
import com.hutnyk.carfix.openingHours.repository.OpeningHoursRepository;
import com.hutnyk.carfix.out.branch.OwnerBranchPortOut;
import com.hutnyk.carfix.review.entity.ReviewEntity;
import com.hutnyk.carfix.review.mapper.ReviewMapper;
import com.hutnyk.carfix.review.repository.ReviewRepository;
import com.hutnyk.carfix.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@PersistenceAdapter
public class OwnerBranchAdapterOut implements OwnerBranchPortOut {

    private static final int LATEST_REVIEWS_PER_BRANCH = 3;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Order.desc("createdAt"));

    private final BranchRepository branchRepository;
    private final BookingRepository bookingRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAvailabilityRepository employeeAvailabilityRepository;
    private final OpeningHoursRepository openingHoursRepository;
    private final OpeningHoursExceptionRepository openingHoursExceptionRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public List<OwnerBranchSummaryView> findSummariesByOwnerId(UserId ownerId, Instant now) {
        List<BranchEntity> branches = branchRepository.findAllWithAddressByOwnerId(ownerId.id());
        if (branches.isEmpty()) {
            return List.of();
        }
        /* "Today" is per branch: each branch is re-zoned to its own tz. */
        Map<UUID, LocalDateTime> localNowByBranch = branches.stream().collect(Collectors.toMap(
                BranchEntity::getId,
                branch -> now.atZone(ZoneId.of(branch.getTz())).toLocalDateTime()));
        Set<UUID> branchIds = localNowByBranch.keySet();
        Set<LocalDate> todays = localNowByBranch.values().stream()
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());

        List<Object[]> bookingRows = bookingRepository.countByBranchDateAndStatus(branchIds, todays);
        List<Object[]> onDutyRows = employeeAvailabilityRepository
                .countDistinctEmployeesByBranchAndDate(branchIds, EmployeeStatus.ACTIVE, todays);
        Map<UUID, Integer> employeesTotal = countsByBranch(
                employeeRepository.countByBranchIdsAndStatus(branchIds, EmployeeStatus.ACTIVE));
        Map<UUID, List<OpeningHoursEntity>> hours = openingHoursRepository
                .findAllByBranchEntityIdIn(branchIds).stream()
                .collect(Collectors.groupingBy(h -> h.getBranchEntity().getId()));
        Map<UUID, List<OpeningHoursExceptionEntity>> exceptions = openingHoursExceptionRepository
                .findAllByBranchEntityIdInAndDateIn(branchIds, todays).stream()
                .collect(Collectors.groupingBy(e -> e.getBranchEntity().getId()));

        return branches.stream()
                .map(branch -> {
                    UUID id = branch.getId();
                    LocalDateTime localNow = localNowByBranch.get(id);
                    LocalDate today = localNow.toLocalDate();
                    return BranchMapper.toOwnerSummaryView(
                            branch,
                            openingSchedule(hours.get(id), exceptions.get(id)).isOpenAt(localNow),
                            sumBookings(bookingRows, id, today, status -> status != BookingStatus.CANCELLED),
                            sumBookings(bookingRows, id, today, status -> status == BookingStatus.COMPLETED),
                            onDuty(onDutyRows, id, today),
                            employeesTotal.getOrDefault(id, 0),
                            latestReviews(id));
                })
                .toList();
    }

    private static OpeningSchedule openingSchedule(
            List<OpeningHoursEntity> hours, List<OpeningHoursExceptionEntity> exceptions) {
        return OpeningSchedule.of(
                nullToEmpty(hours).stream().map(OpeningHoursMapper::toDomain).toList(),
                nullToEmpty(exceptions).stream().map(OpeningHoursMapper::toDomain).toList());
    }

    /* Rows: [branchId, date, status, count] — see BookingRepository.countByBranchDateAndStatus. */
    private static int sumBookings(
            List<Object[]> rows, UUID branchId, LocalDate today, Predicate<BookingStatus> statusFilter) {
        return rows.stream()
                .filter(row -> branchId.equals(row[0]) && today.equals(row[1])
                        && statusFilter.test((BookingStatus) row[2]))
                .mapToInt(row -> ((Number) row[3]).intValue())
                .sum();
    }

    /* Rows: [branchId, date, distinctEmployees] — see EmployeeAvailabilityRepository. */
    private static int onDuty(List<Object[]> rows, UUID branchId, LocalDate today) {
        return rows.stream()
                .filter(row -> branchId.equals(row[0]) && today.equals(row[1]))
                .mapToInt(row -> ((Number) row[2]).intValue())
                .sum();
    }

    /* Rows: [branchId, count]. */
    private static Map<UUID, Integer> countsByBranch(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                row -> (UUID) row[0],
                row -> ((Number) row[1]).intValue()));
    }

    /* Same paged query the public branch page uses; owners have a handful of branches, so one small query each. */
    private List<BranchReviewView> latestReviews(UUID branchId) {
        return reviewRepository
                .findReviewRowsByBranchId(branchId, PageRequest.of(0, LATEST_REVIEWS_PER_BRANCH, NEWEST_FIRST))
                .getContent().stream()
                .map(row -> ReviewMapper.toBranchReviewView((ReviewEntity) row[0], (String) row[1], (String) row[2]))
                .toList();
    }

    private static <T> List<T> nullToEmpty(List<T> list) {
        return list == null ? List.of() : list;
    }
}
