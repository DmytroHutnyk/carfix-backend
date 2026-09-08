package com.hutnyk.carfix.in.booking;

import com.hutnyk.carfix.in.booking.query.OwnerBranchBookingView;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OwnerBranchBookingPortIn {

    List<OwnerBranchBookingView> getBranchDayBookings(String ownerEmail, UUID branchId, LocalDate date);
}
