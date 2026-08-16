package com.hutnyk.carfix.branch.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.in.branch.query.BranchReviewView;
import com.hutnyk.carfix.in.branch.query.BranchView;
import com.hutnyk.carfix.in.branch.query.OwnerBranchSummaryView;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.service.entity.ServiceCategoryEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class BranchMapperTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static BranchEntity branch() {
        CityEntity city = new CityEntity();
        city.setName("Warsaw");

        AddressEntity address = new AddressEntity();
        address.setStreetName("Pulawska");
        address.setBuildingNumber("45");
        address.setLatitude(new BigDecimal("52.180000"));
        address.setLongitude(new BigDecimal("21.020000"));
        address.setCityEntity(city);

        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_ID);
        branch.setName("AutoFix Mokotow");
        branch.setPhoneNumber("+48221234567");
        branch.setEmail("contact@autofix.pl");
        branch.setTz("Europe/Warsaw");
        branch.setRating(new BigDecimal("4.7"));
        branch.setReviewCount(236);
        branch.setDescription("A workshop.");
        branch.setCancellationPolicy("Free cancellation up to 24 hours.");
        branch.setAddressEntity(address);
        return branch;
    }

    private static ServiceEntity service(int id, String name, String categoryName, int categoryId) {
        ServiceCategoryEntity category = new ServiceCategoryEntity();
        category.setId(categoryId);
        category.setName(categoryName);

        ServiceEntity service = new ServiceEntity();
        service.setId(id);
        service.setName(name);
        service.setDurationMinutes((short) 30);
        service.setPrice(new BigDecimal("120.00"));
        service.setServiceCategoryEntity(category);
        return service;
    }

    private static OpeningHoursEntity hours(DayOfWeek day, int startHour) {
        OpeningHoursEntity entity = new OpeningHoursEntity();
        entity.setDayOfWeek(day);
        entity.setStartTime(LocalTime.of(startHour, 0));
        entity.setCloseTime(LocalTime.of(startHour + 8, 0));
        return entity;
    }

    @Test
    public void test_toView_groups_services_by_category_preserving_order() {
        //given — pre-sorted by category name then service name, as the adapter queries them
        List<ServiceEntity> services = List.of(
                service(1, "Brake inspection", "Brake Services", 3),
                service(2, "Brake pad replacement", "Brake Services", 3),
                service(3, "Engine diagnostics", "Engine Services", 5));

        //when
        BranchView view = BranchMapper.toView(branch(), services, List.of(), List.of());

        //then
        assertThat(view.serviceCategories()).hasSize(2);
        assertThat(view.serviceCategories().get(0).name()).isEqualTo("Brake Services");
        assertThat(view.serviceCategories().get(0).services()).hasSize(2);
        assertThat(view.serviceCategories().get(1).categoryId()).isEqualTo(5);
        assertThat(view.serviceCategories().get(1).services().get(0).price())
                .isEqualByComparingTo("120.00");
    }

    @Test
    public void test_toView_sorts_opening_hours_monday_first() {
        //given — deliberately unsorted
        List<OpeningHoursEntity> hours = List.of(
                hours(DayOfWeek.FRIDAY, 9),
                hours(DayOfWeek.MONDAY, 8),
                hours(DayOfWeek.WEDNESDAY, 10));

        //when
        BranchView view = BranchMapper.toView(branch(), List.of(), hours, List.of());

        //then
        assertThat(view.openingHours())
                .extracting(oh -> oh.dayOfWeek())
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
    }

    @Test
    public void test_toView_maps_branch_address_and_brands() {
        //given
        CarBrandEntity brand = new CarBrandEntity();
        brand.setId(7);
        brand.setName("BMW");

        //when
        BranchView view = BranchMapper.toView(branch(), List.of(), List.of(), List.of(brand));

        //then
        assertThat(view.branchId()).isEqualTo(BRANCH_ID);
        assertThat(view.city()).isEqualTo("Warsaw");
        assertThat(view.description()).isEqualTo("A workshop.");
        assertThat(view.cancellationPolicy()).isEqualTo("Free cancellation up to 24 hours.");
        assertThat(view.tz()).isEqualTo("Europe/Warsaw");
        assertThat(view.brands()).hasSize(1);
        assertThat(view.brands().get(0).carBrandId()).isEqualTo(7);
    }

    @Test
    public void test_toOwnerSummaryView_copies_branch_fields_and_precomputed_counts() {
        //given
        BranchEntity entity = branch();
        entity.setStatus(BranchStatus.ACTIVE);
        List<BranchReviewView> reviews = List.of(new BranchReviewView(
                UUID.randomUUID(), 5, "Great", Instant.parse("2026-08-10T10:00:00Z"), "Anna", "Nowak"));

        //when
        OwnerBranchSummaryView view = BranchMapper.toOwnerSummaryView(entity, true, 12, 7, 3, 5, reviews);

        //then
        assertThat(view.branchId()).isEqualTo(BRANCH_ID);
        assertThat(view.name()).isEqualTo("AutoFix Mokotow");
        assertThat(view.status()).isEqualTo(BranchStatus.ACTIVE);
        assertThat(view.streetName()).isEqualTo("Pulawska");
        assertThat(view.buildingNumber()).isEqualTo("45");
        assertThat(view.city()).isEqualTo("Warsaw");
        assertThat(view.rating()).isEqualByComparingTo(new BigDecimal("4.7"));
        assertThat(view.reviewCount()).isEqualTo(236);
        assertThat(view.openNow()).isTrue();
        assertThat(view.bookingsToday()).isEqualTo(12);
        assertThat(view.completedToday()).isEqualTo(7);
        assertThat(view.employeesOnDutyToday()).isEqualTo(3);
        assertThat(view.employeesTotal()).isEqualTo(5);
        assertThat(view.latestReviews()).isEqualTo(reviews);
    }

    @Test
    public void test_toOwnerSummaryView_null_guard() {
        assertThat(BranchMapper.toOwnerSummaryView(null, false, 0, 0, 0, 0, List.of())).isNull();
    }
}
