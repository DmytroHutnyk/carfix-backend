package com.hutnyk.carfix.booking.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.address.entity.CityEntity;
import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchStatus;
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
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public class BookingMapperTest {

    private static final UUID BOOKING_ID = UUID.randomUUID();
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
        diagnostics.setPrice(new BigDecimal("150.00"));

        ServiceEntity airFilter = new ServiceEntity();
        airFilter.setId(2);
        airFilter.setName("Air filter replacement");
        airFilter.setPrice(new BigDecimal("160.00"));

        BookingEntity booking = new BookingEntity();
        booking.setId(BOOKING_ID);
        booking.setDate(LocalDate.of(2030, 6, 12));
        booking.setStatus(BookingStatus.SCHEDULED);
        booking.setStartTime(LocalTime.of(10, 0));
        booking.setEndTime(LocalTime.of(11, 30));
        booking.setBranchEntity(branch);
        booking.setCarProfileEntity(carProfile);
        booking.setServiceEntities(Set.of(diagnostics, airFilter));
        return booking;
    }

    @Test
    public void toViewFlattensBranchVehicleAndServices() {
        BookingView view = BookingMapper.toView(entity());

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
        BookingView view = BookingMapper.toView(entity());

        assertThat(view.safeCancelUntil()).isEqualTo(Instant.parse("2030-06-11T08:00:00Z"));
    }

    @Test
    public void updateEntityCopiesScalarFieldsOnly() {
        BookingEntity target = entity();
        Booking cancelled = BookingMapper.toDomain(target).cancel();

        BookingMapper.updateEntity(target, cancelled);

        assertThat(target.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(target.getDate()).isEqualTo(LocalDate.of(2030, 6, 12));
        assertThat(target.getBranchEntity().getId()).isEqualTo(BRANCH_ID);
        assertThat(target.getCarProfileEntity().getId()).isEqualTo(CAR_PROFILE_ID);
    }
}
