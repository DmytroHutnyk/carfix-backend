package com.hutnyk.carfix.booking.controller;

import com.hutnyk.carfix.booking.dto.response.OwnerBranchBookingResponse;
import com.hutnyk.carfix.booking.mapper.OwnerBranchBookingResponseMapper;
import com.hutnyk.carfix.in.booking.OwnerBranchBookingPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/owner/branches/{branchId}/bookings")
public class OwnerBranchBookingController {

    private final OwnerBranchBookingPortIn ownerBranchBookingPortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OwnerBranchBookingResponse>> getBranchBookings(
            @PathVariable(name = "branchId") UUID branchId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal UserDetails principal) {
        LocalDate effectiveTo = to != null ? to : from;
        List<OwnerBranchBookingResponse> response = ownerBranchBookingPortIn
                .getBranchBookings(principal.getUsername(), branchId, from, effectiveTo).stream()
                .map(OwnerBranchBookingResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
}
