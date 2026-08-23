package com.hutnyk.carfix.booking.mapper;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.booking.BookingPricing;
import com.hutnyk.carfix.booking.BookingSegment;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentEntity;
import com.hutnyk.carfix.booking.entity.BookingSegmentKey;
import com.hutnyk.carfix.branch.BranchId;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carCatalog.entity.CarModelEntity;
import com.hutnyk.carfix.carCatalog.entity.ModelVersionEntity;
import com.hutnyk.carfix.carProfile.CarProfileId;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.service.entity.ServiceEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashSet;
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
                CarProfileId.of(e.getCarProfileEntity().getId()),
                e.getSegments() == null ? List.of() : e.getSegments().stream()
                        .map(s -> BookingSegment.of(
                                s.getKey().getServiceId(), s.getStartTime(), s.getEndTime(), s.getPrice()))
                        .toList()
        );
    }

    public static BookingView toView(BookingEntity e, Instant now) {
        if (e == null) return null;
        BranchEntity branch = e.getBranchEntity();
        ZoneId branchZone = ZoneId.of(branch.getTz());
        Booking booking = toDomain(e);
        AddressEntity address = branch.getAddressEntity();
        CarProfileEntity carProfile = e.getCarProfileEntity();
        ModelVersionEntity version = carProfile.getModelVersionEntity();
        CarModelEntity model = version.getCarModelEntity();

        List<BookingServiceView> services = e.getSegments().stream()
                .map(s -> new BookingServiceView(s.getServiceEntity().getName(), s.getPrice()))
                .sorted(Comparator.comparing(BookingServiceView::name))
                .toList();

        BigDecimal totalPrice = BookingPricing.total(
                services.stream().map(BookingServiceView::price).toList());

        return new BookingView(
                e.getId(),
                e.getDate(),
                e.getStartTime(),
                e.getEndTime(),
                booking.effectiveStatus(now.atZone(branchZone).toLocalDateTime()),
                booking.safeCancelUntil(branchZone, branch.getCancellationPolicy().notice()),
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

    public static BookingEntity toEntity(Booking b, BranchEntity branch, CarProfileEntity carProfile) {
        if (b == null) return null;
        return new BookingEntity(
                b.getId().id(),
                b.getDate(),
                b.getStatus(),
                b.getStartTime(),
                b.getEndTime(),
                branch,
                carProfile,
                new HashSet<>()
        );
    }

    public static BookingSegmentEntity toSegmentEntity(BookingSegment s, BookingEntity booking, ServiceEntity service) {
        if (s == null) return null;
        return new BookingSegmentEntity(
                new BookingSegmentKey(booking.getId(), service.getId()),
                booking,
                service,
                s.startTime(),
                s.endTime(),
                s.price()
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
