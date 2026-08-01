package com.hutnyk.carfix.booking.mapper;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import com.hutnyk.carfix.carCatalog.entity.ModelGenerationEntity;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

public class BookingMapper {

    public static Booking toDomain(BookingEntity e) {
        if (e == null) return null;
        return Booking.of(
                BookingId.of(e.getId()),
                e.getDate(),
                e.getStatus(),
                e.getStartTime(),
                e.getEndTime(),
                BranchId.of(e.getBranchEntity().getId()),
                CarProfileId.of(e.getCarProfileEntity().getId())
        );
    }

    public static BookingView toView(BookingEntity e) {
        if (e == null) return null;
        BranchEntity branch = e.getBranchEntity();
        AddressEntity address = branch.getAddressEntity();
        CarProfileEntity carProfile = e.getCarProfileEntity();
        ModelGenerationEntity generation = carProfile.getModelGenerationEntity();
        CarModelEntity model = generation.getCarModelEntity();

        List<BookingServiceView> services = e.getServiceEntities().stream()
                .map(s -> new BookingServiceView(s.getName(), s.getPrice()))
                .sorted(Comparator.comparing(BookingServiceView::name))
                .toList();

        BigDecimal totalPrice = services.stream()
                .map(BookingServiceView::price)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BookingView(
                e.getId(),
                e.getDate(),
                e.getStartTime(),
                e.getEndTime(),
                e.getStatus(),
                toDomain(e).safeCancelUntil(ZoneId.of(branch.getTz())),
                branch.getId(),
                branch.getName(),
                branch.getPhoneNumber(),
                branch.getEmail(),
                address.getStreetName(),
                address.getBuildingNumber(),
                address.getCityEntity().getName(),
                carProfile.getId(),
                carProfile.getName(),
                model.getCarBrandEntity().getName(),
                model.getName(),
                carProfile.getPlates(),
                services,
                totalPrice
        );
    }

    public static void updateEntity(BookingEntity e, Booking b) {
        if (e == null || b == null) return;
        e.setDate(b.getDate());
        e.setStatus(b.getStatus());
        e.setStartTime(b.getStartTime());
        e.setEndTime(b.getEndTime());
    }
}
