package com.hutnyk.carfix.serviceBay.controller;

import com.hutnyk.carfix.in.serviceBay.OwnerServiceBayPortIn;
import com.hutnyk.carfix.serviceBay.dto.request.CreateServiceBayRequest;
import com.hutnyk.carfix.serviceBay.dto.request.UpdateServiceBayRequest;
import com.hutnyk.carfix.serviceBay.dto.response.ServiceBayResponse;
import com.hutnyk.carfix.serviceBay.dto.response.ServiceBayTypeResponse;
import com.hutnyk.carfix.serviceBay.mapper.CreateServiceBayCommandMapper;
import com.hutnyk.carfix.serviceBay.mapper.ServiceBayResponseMapper;
import com.hutnyk.carfix.serviceBay.mapper.UpdateServiceBayCommandMapper;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/owner/branches/{branchId}/service-bays")
public class OwnerServiceBayController {

    private final OwnerServiceBayPortIn ownerServiceBayPortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ServiceBayResponse>> getServiceBays(
            @PathVariable(name = "branchId") UUID branchId,
            @AuthenticationPrincipal UserDetails principal) {
        List<ServiceBayResponse> response = ownerServiceBayPortIn
                .getServiceBays(principal.getUsername(), branchId).stream()
                .map(ServiceBayResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/types")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<ServiceBayTypeResponse>> getServiceBayTypes(
            @PathVariable(name = "branchId") UUID branchId,
            @AuthenticationPrincipal UserDetails principal) {
        List<ServiceBayTypeResponse> response = ownerServiceBayPortIn
                .getServiceBayTypes(principal.getUsername(), branchId).stream()
                .map(ServiceBayResponseMapper::toTypeResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceBayResponse> createServiceBay(
            @PathVariable(name = "branchId") UUID branchId,
            @Valid @RequestBody CreateServiceBayRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ServiceBayResponse response = ServiceBayResponseMapper.toResponse(ownerServiceBayPortIn.createServiceBay(
                principal.getUsername(), branchId, CreateServiceBayCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{bayId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ServiceBayResponse> updateServiceBay(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "bayId") Integer bayId,
            @Valid @RequestBody UpdateServiceBayRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        ServiceBayResponse response = ServiceBayResponseMapper.toResponse(ownerServiceBayPortIn.updateServiceBay(
                principal.getUsername(), branchId, bayId, UpdateServiceBayCommandMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }
}
