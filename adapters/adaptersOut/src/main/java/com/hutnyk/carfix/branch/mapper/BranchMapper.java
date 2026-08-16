package com.hutnyk.carfix.branch.mapper;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.in.branch.query.BranchBrandView;
import com.hutnyk.carfix.in.branch.query.BranchOpeningHoursView;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.BranchServiceCategoryView;
import com.hutnyk.carfix.in.branch.query.BranchServiceView;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class BranchMapper {

    private BranchMapper() {
    }

    public static BranchView toView(
            BranchEntity branch,
            List<ServiceEntity> services,
            List<OpeningHoursEntity> openingHours,
            List<CarBrandEntity> brands) {
        if (branch == null) {
            return null;
        }
        AddressEntity address = branch.getAddressEntity();
        return new BranchView(
                branch.getId(),
                branch.getName(),
                branch.getPhoneNumber(),
                branch.getEmail(),
                branch.getDescription(),
                branch.getCancellationPolicy(),
                branch.getRating(),
                branch.getReviewCount(),
                address.getStreetName(),
                address.getBuildingNumber(),
                address.getCityEntity().getName(),
                address.getLatitude(),
                address.getLongitude(),
                address.getGooglePlaceId(),
                branch.getTz(),
                brands.stream()
                        .map(brand -> new BranchBrandView(brand.getId(), brand.getName()))
                        .toList(),
                openingHours.stream()
                        .sorted(Comparator
                                .comparingInt((OpeningHoursEntity oh) -> oh.getDayOfWeek().ordinal())
                                .thenComparing(OpeningHoursEntity::getStartTime))
                        .map(oh -> new BranchOpeningHoursView(
                                oh.getDayOfWeek(), oh.getStartTime(), oh.getCloseTime()))
                        .toList(),
                groupByCategory(services));
    }


    public static OwnerBranchSummaryView toOwnerSummaryView(
            BranchEntity branch,
            boolean openNow,
            int bookingsToday,
            int completedToday,
            int employeesOnDutyToday,
            int employeesTotal,
            List<BranchReviewView> latestReviews) {
        if (branch == null) {
            return null;
        }
        AddressEntity address = branch.getAddressEntity();
        return new OwnerBranchSummaryView(
                branch.getId(),
                branch.getName(),
                branch.getStatus(),
                address.getStreetName(),
                address.getBuildingNumber(),
                address.getCityEntity().getName(),
                branch.getRating(),
                branch.getReviewCount(),
                openNow,
                bookingsToday,
                completedToday,
                employeesOnDutyToday,
                employeesTotal,
                latestReviews);
    }

    private static List<BranchServiceCategoryView> groupByCategory(List<ServiceEntity> services) {
        LinkedHashMap<ServiceCategoryEntity, List<BranchServiceView>> byCategory = new LinkedHashMap<>();
        for (ServiceEntity service : services) {
            byCategory.computeIfAbsent(service.getServiceCategoryEntity(), category -> new ArrayList<>())
                    .add(new BranchServiceView(
                            service.getId(),
                            service.getName(),
                            service.getDescription(),
                            service.getDurationMinutes(),
                            service.getPrice()));
        }
        return byCategory.entrySet().stream()
                .map(entry -> new BranchServiceCategoryView(
                        entry.getKey().getId(), entry.getKey().getName(), List.copyOf(entry.getValue())))
                .toList();
    }
}
