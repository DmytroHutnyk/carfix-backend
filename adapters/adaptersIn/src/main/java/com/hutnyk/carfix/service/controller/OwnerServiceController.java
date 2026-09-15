package com.hutnyk.carfix.service.controller;

import com.hutnyk.carfix.in.service.OwnerServicePortIn;
import com.hutnyk.carfix.service.dto.request.CreateServiceRequest;
import com.hutnyk.carfix.service.dto.request.UpdateServiceRequest;
import com.hutnyk.carfix.service.dto.response.OwnerServiceResponse;
import com.hutnyk.carfix.service.mapper.CreateServiceCommandMapper;
import com.hutnyk.carfix.service.mapper.ServiceResponseMapper;
import com.hutnyk.carfix.service.mapper.UpdateServiceCommandMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/owner/branches/{branchId}/services")
public class OwnerServiceController {

    private final OwnerServicePortIn ownerServicePortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OwnerServiceResponse>> getServices(
            @PathVariable(name = "branchId") UUID branchId,
            @AuthenticationPrincipal UserDetails principal) {
        List<OwnerServiceResponse> response = ownerServicePortIn
                .getServices(principal.getUsername(), branchId).stream()
                .map(ServiceResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerServiceResponse> createService(
            @PathVariable(name = "branchId") UUID branchId,
            @Valid @RequestBody CreateServiceRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        OwnerServiceResponse response = ServiceResponseMapper.toResponse(ownerServicePortIn.createService(
                principal.getUsername(), branchId, CreateServiceCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{serviceId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerServiceResponse> updateService(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "serviceId") Integer serviceId,
            @Valid @RequestBody UpdateServiceRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        OwnerServiceResponse response = ServiceResponseMapper.toResponse(ownerServicePortIn.updateService(
                principal.getUsername(), branchId, serviceId, UpdateServiceCommandMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{serviceId}/activate")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerServiceResponse> activateService(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "serviceId") Integer serviceId,
            @AuthenticationPrincipal UserDetails principal) {
        OwnerServiceResponse response = ServiceResponseMapper.toResponse(
                ownerServicePortIn.activateService(principal.getUsername(), branchId, serviceId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{serviceId}/suspend")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerServiceResponse> suspendService(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "serviceId") Integer serviceId,
            @AuthenticationPrincipal UserDetails principal) {
        OwnerServiceResponse response = ServiceResponseMapper.toResponse(
                ownerServicePortIn.suspendService(principal.getUsername(), branchId, serviceId));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{serviceId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> deleteService(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "serviceId") Integer serviceId,
            @AuthenticationPrincipal UserDetails principal) {
        ownerServicePortIn.deleteService(principal.getUsername(), branchId, serviceId);
        return ResponseEntity.noContent().build();
    }
}
