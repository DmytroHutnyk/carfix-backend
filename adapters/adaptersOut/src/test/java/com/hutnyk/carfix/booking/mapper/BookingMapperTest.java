package com.hutnyk.carfix.booking.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingSegment;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentKey;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.branch.CancellationPolicy;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarBrandEntity;
import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public class BookingMapperTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
    /* 2030-06-12 09:59 in Europe/Warsaw: one minute before the booked slot starts. */
    private static final Instant BEFORE_START = Instant.parse("2030-06-12T07:59:00Z");
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID CAR_PROFILE_ID = UUID.randomUUID();

    private static BookingEntity entity() {
        CityEntity city = new CityEntity();
        city.setName("Warszawa");

        AddressEntity address = new AddressEntity();
        address.setStreetName("Górczewska");
        address.setBuildingNumber("110");
        address.setCityEntity(city);

        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_ID);
        branch.setName("SpeedCare Wola");
        branch.setPhoneNumber("+48123456789");
        branch.setEmail("wola@speedcare.pl");
        branch.setStatus(BranchStatus.ACTIVE);
        branch.setTz("Europe/Warsaw");
        branch.setCancellationPolicy(CancellationPolicy.MODERATE);
        branch.setAddressEntity(address);

        CarBrandEntity brand = new CarBrandEntity();
        brand.setName("BMW");

        CarModelEntity model = new CarModelEntity();
        model.setName("X5");
        model.setCarBrandEntity(brand);

        ModelVersionEntity version = new ModelVersionEntity();
        version.setName("G05");
        version.setCarModelEntity(model);

        CarProfileEntity carProfile = new CarProfileEntity();
        carProfile.setId(CAR_PROFILE_ID);
        carProfile.setName("Weekend Car");
        carProfile.setPlates("KR 67890");
        carProfile.setModelVersionEntity(version);

        ServiceEntity diagnostics = new ServiceEntity();
        diagnostics.setId(1);
        diagnostics.setName("Diagnostics");
        diagnostics.setPrice(new BigDecimal("999.00"));

        ServiceEntity airFilter = new ServiceEntity();
        airFilter.setId(2);
        airFilter.setName("Air filter replacement");
        airFilter.setPrice(new BigDecimal("999.00"));

        BookingEntity booking = new BookingEntity();
        booking.setId(BOOKING_ID);
        booking.setDate(LocalDate.of(2030, 6, 12));
        booking.setStatus(BookingStatus.SCHEDULED);
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 30));
        booking.setBranchEntity(branch);
        booking.setCarProfileEntity(carProfile);
        booking.setSegments(Set.of(
                new BookingSegmentEntity(new BookingSegmentKey(BOOKING_ID, 1), booking, diagnostics,
                        LocalTime.of(10, 0), LocalTime.of(10, 30), new BigDecimal("150.00")),
                new BookingSegmentEntity(new BookingSegmentKey(BOOKING_ID, 2), booking, airFilter,
                        LocalTime.of(10, 30), LocalTime.of(11, 30), new BigDecimal("160.00"))));
        return booking;
    }

    @Test
    public void toViewFlattensBranchVehicleAndServices() {
        BookingView view = BookingMapper.toView(entity(), BEFORE_START);

        assertThat(view.id()).isEqualTo(BOOKING_ID);
        assertThat(view.status()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(view.branchId()).isEqualTo(BRANCH_ID);
        assertThat(view.branchName()).isEqualTo("SpeedCare Wola");
        assertThat(view.branchPhoneNumber()).isEqualTo("+48123456789");
        assertThat(view.branchEmail()).isEqualTo("wola@speedcare.pl");
        assertThat(view.streetName()).isEqualTo("Górczewska");
        assertThat(view.buildingNumber()).isEqualTo("110");
        assertThat(view.city()).isEqualTo("Warszawa");
        assertThat(view.carProfileId()).isEqualTo(CAR_PROFILE_ID);
        assertThat(view.carProfileName()).isEqualTo("Weekend Car");
        assertThat(view.brandName()).isEqualTo("BMW");
        assertThat(view.modelName()).isEqualTo("X5");
        assertThat(view.plates()).isEqualTo("KR 67890");
        assertThat(view.services())
                .containsExactly(
                        new BookingServiceView("Air filter replacement", new BigDecimal("160.00")),
                        new BookingServiceView("Diagnostics", new BigDecimal("150.00")));
        assertThat(view.totalPrice()).isEqualByComparingTo(new BigDecimal("310.00"));
    }

    @Test
    public void toViewComputesSafeCancelUntilInBranchZone() {
        BookingView view = BookingMapper.toView(entity(), BEFORE_START);

        assertThat(view.safeCancelUntil()).isEqualTo(Instant.parse("2030-06-11T08:00:00Z"));
    }

    @Test
    public void toViewTakesTheNoticeWindowFromTheBranchPolicy() {
        BookingEntity entity = entity();
        entity.getBranchEntity().setCancellationPolicy(CancellationPolicy.STRICT);

        BookingView view = BookingMapper.toView(entity, BEFORE_START);

        assertThat(view.safeCancelUntil()).isEqualTo(Instant.parse("2030-06-10T08:00:00Z"));
    }

    @Test
    public void toViewDerivesTheStatusFromTheBranchClock() {
        BookingEntity entity = entity();

        assertThat(BookingMapper.toView(entity, Instant.parse("2030-06-12T08:00:00Z")).status())
                .isEqualTo(BookingStatus.IN_PROGRESS);
        assertThat(BookingMapper.toView(entity, Instant.parse("2030-06-12T09:30:00Z")).status())
                .isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    public void toViewKeepsAStoredNoShowWhateverTheClockSays() {
        BookingEntity entity = entity();
        entity.setStatus(BookingStatus.NO_SHOW);

        assertThat(BookingMapper.toView(entity, BEFORE_START).status()).isEqualTo(BookingStatus.NO_SHOW);
    }

    @Test
    public void toDomainCarriesSegmentsSortedByStart() {
        Booking booking = BookingMapper.toDomain(entity());

        assertThat(booking.getSegments()).extracting(BookingSegment::serviceId).containsExactly(1, 2);
        assertThat(booking.getSegments().getFirst().price()).isEqualByComparingTo("150.00");
    }

    @Test
    public void updateEntityCopiesScalarFieldsOnly() {
        BookingEntity target = entity();
        Booking cancelled = BookingMapper.toDomain(target).cancel(LocalDateTime.of(2030, 6, 12, 9, 59));

        BookingMapper.updateEntity(target, cancelled);

        assertThat(target.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(target.getDate()).isEqualTo(LocalDate.of(2030, 6, 12));
        assertThat(target.getBranchEntity().getId()).isEqualTo(BRANCH_ID);
        assertThat(target.getCarProfileEntity().getId()).isEqualTo(CAR_PROFILE_ID);
    }

    @Test
    public void toEntityCopiesScalarsAndReferencesWithoutSegments() {
        Booking booking = BookingMapper.toDomain(entity());
        BranchEntity branch = new BranchEntity();
        branch.setId(BRANCH_ID);
        CarProfileEntity carProfile = new CarProfileEntity();
        carProfile.setId(CAR_PROFILE_ID);

        BookingEntity e = BookingMapper.toEntity(booking, branch, carProfile);

        assertThat(e.getId()).isEqualTo(BOOKING_ID);
        assertThat(e.getDate()).isEqualTo(LocalDate.of(2030, 6, 12));
        assertThat(e.getStatus()).isEqualTo(BookingStatus.SCHEDULED);
        assertThat(e.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(e.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(e.getBranchEntity()).isSameAs(branch);
        assertThat(e.getCarProfileEntity()).isSameAs(carProfile);
        assertThat(e.getSegments()).isEmpty();
    }

    @Test
    public void toSegmentEntityBuildsTheCompositeKeyFromBothReferences() {
        BookingEntity booking = new BookingEntity();
        booking.setId(BOOKING_ID);
        ServiceEntity service = new ServiceEntity();
        service.setId(27);
        BookingSegment segment = BookingSegment.of(27, LocalTime.of(10, 30), LocalTime.of(11, 30), new BigDecimal("160.00"));

        BookingSegmentEntity e = BookingMapper.toSegmentEntity(segment, booking, service);

        assertThat(e.getKey()).isEqualTo(new BookingSegmentKey(BOOKING_ID, 27));
        assertThat(e.getBookingEntity()).isSameAs(booking);
        assertThat(e.getServiceEntity()).isSameAs(service);
        assertThat(e.getStartTime()).isEqualTo(LocalTime.of(10, 30));
        assertThat(e.getEndTime()).isEqualTo(LocalTime.of(11, 30));
        assertThat(e.getPrice()).isEqualByComparingTo("160.00");
    }
}
