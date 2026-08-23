package com.hutnyk.carfix.branch.controller;

import com.hutnyk.carfix.branch.dto.request.RegisterBranchRequest;
import com.hutnyk.carfix.branch.dto.request.UpdateBranchOverviewRequest;
import com.hutnyk.carfix.branch.dto.response.BranchRegistrationResponse;
import com.hutnyk.carfix.branch.dto.response.OwnerBranchDetailResponse;
import com.hutnyk.carfix.branch.dto.response.OwnerBranchSummaryResponse;
import com.hutnyk.carfix.branch.mapper.BranchResponseMapper;
import com.hutnyk.carfix.branch.mapper.RegisterBranchCommandMapper;
import com.hutnyk.carfix.branch.mapper.UpdateBranchOverviewCommandMapper;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
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
@RequestMapping("/api/owner/branches")
public class OwnerBranchController {

    private final OwnerBranchPortIn ownerBranchPortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<OwnerBranchSummaryResponse>> getMyBranches(
            @AuthenticationPrincipal UserDetails principal) {
        List<OwnerBranchSummaryResponse> response = ownerBranchPortIn
                .getMyBranchSummaries(principal.getUsername()).stream()
                .map(BranchResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{branchId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerBranchDetailResponse> getMyBranch(@PathVariable(name = "branchId") UUID branchId,
                                                                 @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(BranchResponseMapper.toDetailResponse(
                ownerBranchPortIn.getMyBranch(principal.getUsername(), branchId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BranchRegistrationResponse> registerBranch(@Valid @RequestBody RegisterBranchRequest request,
                                                                     @AuthenticationPrincipal UserDetails principal) {
        BranchRegistrationResponse response = BranchResponseMapper.toRegistrationResponse(
                ownerBranchPortIn.registerBranch(principal.getUsername(), RegisterBranchCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{branchId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<OwnerBranchDetailResponse> updateBranchOverview(
            @PathVariable(name = "branchId") UUID branchId,
            @Valid @RequestBody UpdateBranchOverviewRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(BranchResponseMapper.toDetailResponse(
                ownerBranchPortIn.updateBranchOverview(principal.getUsername(), branchId,
                        UpdateBranchOverviewCommandMapper.toCommand(request))));
    }
}
