package com.hutnyk.carfix.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.hutnyk.carfix.employee.EmployeeBooking;
import com.hutnyk.carfix.equipment.EquipmentBooking;
import com.hutnyk.carfix.scheduling.SegmentPlan;
import com.hutnyk.carfix.scheduling.TimeRange;
import com.hutnyk.carfix.scheduling.VisitPlan;
import com.hutnyk.carfix.serviceBay.ServiceBayBooking;
import com.hutnyk.carfix.user.UserId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BookingOccupancyTest {

    private static final LocalDate DATE = LocalDate.of(2030, 6, 12);
    private static final BookingId BOOKING_ID = BookingId.genId();
    private static final UserId ANNA = UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UserId JAN = UserId.of(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    private static LocalDateTime at(int h, int m) {
        return DATE.atTime(h, m);
    }

    private static VisitPlan twoSegmentPlan() {
        SegmentPlan first = new SegmentPlan(11, TimeRange.of(at(9, 0), at(9, 50)),
                Map.of(1, ANNA, 2, JAN), Map.of(7, 500));
        SegmentPlan second = new SegmentPlan(27, TimeRange.of(at(10, 0), at(11, 30)),
                Map.of(3, ANNA), Map.of());
        return new VisitPlan(100, List.of(first, second));
    }

    @Test
    public void bayRowCoversTheWholeSpanIncludingTheGap() {
        BookingOccupancy occupancy = BookingOccupancy.of(BOOKING_ID, twoSegmentPlan());

        assertThat(occupancy.bays()).hasSize(1);
        ServiceBayBooking bay = occupancy.bays().getFirst();
        assertThat(bay.getServiceBayId()).isEqualTo(100);
        assertThat(bay.getDate()).isEqualTo(DATE);
        assertThat(bay.getBookedTime()).isEqualTo(TimeRange.of(at(9, 0), at(11, 30)));
        assertThat(bay.getBookingId()).isEqualTo(BOOKING_ID);
        assertThat(bay.getId()).isNull();
    }

    @Test
    public void oneEmployeeRowPerFilledSlotPerSegment() {
        BookingOccupancy occupancy = BookingOccupancy.of(BOOKING_ID, twoSegmentPlan());

        assertThat(occupancy.employees())
                .extracting(EmployeeBooking::getEmployeeId, EmployeeBooking::getBookedTime)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(ANNA, TimeRange.of(at(9, 0), at(9, 50))),
                        org.assertj.core.groups.Tuple.tuple(JAN, TimeRange.of(at(9, 0), at(9, 50))),
                        org.assertj.core.groups.Tuple.tuple(ANNA, TimeRange.of(at(10, 0), at(11, 30))));
        assertThat(occupancy.employees()).allSatisfy(e -> {
            assertThat(e.getDate()).isEqualTo(DATE);
            assertThat(e.getBookingId()).isEqualTo(BOOKING_ID);
        });
    }

    @Test
    public void oneEquipmentRowPerFilledSlotPerSegment() {
        BookingOccupancy occupancy = BookingOccupancy.of(BOOKING_ID, twoSegmentPlan());

        assertThat(occupancy.equipment())
                .extracting(EquipmentBooking::getEquipmentId, EquipmentBooking::getBookedTime)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(500, TimeRange.of(at(9, 0), at(9, 50))));
    }

    @Test
    public void employeeRowsAreOrderedByEmployeeThenStartAcrossSegments() {
        //given
        SegmentPlan first = new SegmentPlan(11, TimeRange.of(at(9, 0), at(9, 50)), Map.of(1, JAN), Map.of());
        SegmentPlan second = new SegmentPlan(27, TimeRange.of(at(10, 0), at(11, 30)), Map.of(2, JAN, 3, ANNA), Map.of());
        //when
        BookingOccupancy occupancy = BookingOccupancy.of(BOOKING_ID, new VisitPlan(100, List.of(first, second)));
        //then
        assertThat(occupancy.employees())
                .extracting(EmployeeBooking::getEmployeeId, e -> e.getBookedTime().lower())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(ANNA, at(10, 0)),
                        org.assertj.core.groups.Tuple.tuple(JAN, at(9, 0)),
                        org.assertj.core.groups.Tuple.tuple(JAN, at(10, 0)));
    }

    @Test
    public void equipmentRowsAreOrderedByUnitThenStartAcrossSegments() {
        //given
        SegmentPlan first = new SegmentPlan(11, TimeRange.of(at(9, 0), at(9, 50)), Map.of(1, ANNA), Map.of(7, 700, 8, 500));
        SegmentPlan second = new SegmentPlan(27, TimeRange.of(at(10, 0), at(11, 30)), Map.of(2, ANNA), Map.of(9, 500));
        //when
        BookingOccupancy occupancy = BookingOccupancy.of(BOOKING_ID, new VisitPlan(100, List.of(first, second)));
        //then
        assertThat(occupancy.equipment())
                .extracting(EquipmentBooking::getEquipmentId, e -> e.getBookedTime().lower())
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(500, at(9, 0)),
                        org.assertj.core.groups.Tuple.tuple(500, at(10, 0)),
                        org.assertj.core.groups.Tuple.tuple(700, at(9, 0)));
    }
}
