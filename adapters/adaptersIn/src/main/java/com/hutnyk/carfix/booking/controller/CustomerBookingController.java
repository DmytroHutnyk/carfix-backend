package com.hutnyk.carfix.booking.controller;

import com.hutnyk.carfix.booking.dto.request.CreateBookingRequest;
import com.hutnyk.carfix.booking.dto.response.CustomerBookingResponse;
import com.hutnyk.carfix.booking.mapper.BookingResponseMapper;
import com.hutnyk.carfix.booking.mapper.CreateBookingCommandMapper;
import com.hutnyk.carfix.in.booking.BookingPortIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/bookings")
public class CustomerBookingController {

    private final BookingPortIn bookingPortIn;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CustomerBookingResponse>> getMyBookings(@AuthenticationPrincipal UserDetails principal) {
        List<CustomerBookingResponse> response = bookingPortIn.getMyBookings(principal.getUsername()).stream()
                .map(BookingResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerBookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                                @AuthenticationPrincipal UserDetails principal) {
        CustomerBookingResponse response = BookingResponseMapper.toResponse(
                bookingPortIn.createBooking(principal.getUsername(), CreateBookingCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerBookingResponse> cancelBooking(@PathVariable(name = "id") UUID bookingId,
                                                                @AuthenticationPrincipal UserDetails principal) {
        CustomerBookingResponse response = BookingResponseMapper.toResponse(
                bookingPortIn.cancelBooking(principal.getUsername(), bookingId));
        return ResponseEntity.ok(response);
    }
}
