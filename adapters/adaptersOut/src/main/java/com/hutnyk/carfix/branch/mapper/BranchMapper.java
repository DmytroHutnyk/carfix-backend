package com.hutnyk.carfix.branch.mapper;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.branch.Branch;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.in.branch.query.BranchBrandView;
import com.hutnyk.carfix.in.branch.query.BranchOpeningHoursView;
import com.hutnyk.carfix.in.branch.query.BranchServiceCategoryView;
import com.hutnyk.carfix.in.branch.query.BranchServiceView;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.openingHours.OpeningHours;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import com.hutnyk.carfix.user.UserId;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public final class BranchMapper {

    private BranchMapper() {
    }

    public static BranchEntity toEntity(Branch branch, AddressEntity address) {
        if (branch == null) {
            return null;
        }
        BranchEntity entity = new BranchEntity();
        entity.setId(branch.getId().id());
        entity.setName(branch.getName());
        entity.setPhoneNumber(branch.getPhoneNumber());
        entity.setEmail(branch.getEmail());
        entity.setStatus(branch.getStatus());
        entity.setTz(branch.getTz().getId());
        entity.setAddressEntity(address);
        entity.setOwnerId(branch.getOwnerId().id());
        return entity;
    }

    public static Branch toDomain(BranchEntity entity) {
        if (entity == null) {
            return null;
        }
        return Branch.of(
                BranchId.of(entity.getId()),
                entity.getName(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getStatus(),
                ZoneId.of(entity.getTz()),
                entity.getAddressEntity().getId(),
                UserId.of(entity.getOwnerId()));
    }

    public static OpeningHoursEntity toEntity(OpeningHours hours, BranchEntity branch) {
        if (hours == null) {
            return null;
        }
        OpeningHoursEntity entity = new OpeningHoursEntity();
        entity.setId(hours.getId());
        entity.setDayOfWeek(hours.getDayOfWeek());
        entity.setStartTime(hours.getStartTime());
        entity.setCloseTime(hours.getCloseTime());
        entity.setMode(hours.getMode());
        entity.setBranchEntity(branch);
        return entity;
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
