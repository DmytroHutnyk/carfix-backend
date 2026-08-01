package com.hutnyk.carfix.carProfile.controller;

import com.hutnyk.carfix.carProfile.dto.request.CreateCarProfileRequest;
import com.hutnyk.carfix.carProfile.dto.request.UpdateCarProfileRequest;
import com.hutnyk.carfix.carProfile.dto.response.CarProfileResponse;
import com.hutnyk.carfix.carProfile.mapper.CarProfileResponseMapper;
import com.hutnyk.carfix.carProfile.mapper.CreateCarProfileCommandMapper;
import com.hutnyk.carfix.carProfile.mapper.UpdateCarProfileCommandMapper;
import com.hutnyk.carfix.in.CarProfilePortIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/car-profiles")
public class CustomerCarProfileController {

    private final CarProfilePortIn carProfilePortIn;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CarProfileResponse>> getMyCarProfiles(@AuthenticationPrincipal UserDetails principal) {
        List<CarProfileResponse> response = carProfilePortIn.getMyCarProfiles(principal.getUsername()).stream()
                .map(CarProfileResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CarProfileResponse> createCarProfile(@Valid @RequestBody CreateCarProfileRequest request,
                                                               @AuthenticationPrincipal UserDetails principal) {
        CarProfileResponse response = CarProfileResponseMapper.toResponse(
                carProfilePortIn.createCarProfile(principal.getUsername(), CreateCarProfileCommandMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CarProfileResponse> updateCarProfile(@PathVariable(name = "id") UUID carProfileId,
                                                               @Valid @RequestBody UpdateCarProfileRequest request,
                                                               @AuthenticationPrincipal UserDetails principal) {
        CarProfileResponse response = CarProfileResponseMapper.toResponse(
                carProfilePortIn.updateCarProfile(principal.getUsername(), carProfileId, UpdateCarProfileCommandMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteCarProfile(@PathVariable(name = "id") UUID carProfileId,
                                                 @AuthenticationPrincipal UserDetails principal) {
        carProfilePortIn.deleteCarProfile(principal.getUsername(), carProfileId);
        return ResponseEntity.noContent().build();
    }
}
