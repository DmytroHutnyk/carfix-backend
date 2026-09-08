package com.hutnyk.carfix.equipment.controller;

import com.hutnyk.carfix.equipment.dto.request.CreateEquipmentRequest;
import com.hutnyk.carfix.equipment.dto.request.UpdateEquipmentRequest;
import com.hutnyk.carfix.equipment.dto.response.EquipmentResponse;
import com.hutnyk.carfix.equipment.mapper.EquipmentCommandMapper;
import com.hutnyk.carfix.equipment.mapper.EquipmentResponseMapper;
import com.hutnyk.carfix.in.equipment.OwnerEquipmentPortIn;
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
@RequestMapping("/api/owner/branches/{branchId}/equipment")
public class OwnerEquipmentController {

    private final OwnerEquipmentPortIn ownerEquipmentPortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<EquipmentResponse>> getEquipment(
            @PathVariable(name = "branchId") UUID branchId,
            @AuthenticationPrincipal UserDetails principal) {
        List<EquipmentResponse> response = ownerEquipmentPortIn
                .getEquipment(principal.getUsername(), branchId).stream()
                .map(EquipmentResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EquipmentResponse> createEquipment(
            @PathVariable(name = "branchId") UUID branchId,
            @Valid @RequestBody CreateEquipmentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        EquipmentResponse response = EquipmentResponseMapper.toResponse(
                ownerEquipmentPortIn.createEquipment(principal.getUsername(), branchId,
                        EquipmentCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{equipmentId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EquipmentResponse> updateEquipment(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "equipmentId") Integer equipmentId,
            @Valid @RequestBody UpdateEquipmentRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        EquipmentResponse response = EquipmentResponseMapper.toResponse(
                ownerEquipmentPortIn.updateEquipment(principal.getUsername(), branchId, equipmentId,
                        EquipmentCommandMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }
}
