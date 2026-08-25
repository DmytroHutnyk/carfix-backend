package com.hutnyk.carfix.booking.controller;

import com.hutnyk.carfix.in.booking.OwnerBookingPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/owner/bookings")
public class OwnerBookingController {

    private final OwnerBookingPortIn ownerBookingPortIn;

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> markNoShow(@PathVariable(name = "id") UUID bookingId,
                                           @AuthenticationPrincipal UserDetails principal) {
        ownerBookingPortIn.markNoShow(principal.getUsername(), bookingId);
        return ResponseEntity.noContent().build();
    }
}
