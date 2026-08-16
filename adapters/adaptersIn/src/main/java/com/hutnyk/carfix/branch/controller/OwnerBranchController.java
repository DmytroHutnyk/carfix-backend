package com.hutnyk.carfix.branch.controller;

import com.hutnyk.carfix.branch.dto.request.RegisterBranchRequest;
import com.hutnyk.carfix.branch.dto.response.BranchRegistrationResponse;
import com.hutnyk.carfix.branch.dto.response.OwnerBranchSummaryResponse;
import com.hutnyk.carfix.branch.mapper.BranchResponseMapper;
import com.hutnyk.carfix.branch.mapper.RegisterBranchCommandMapper;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<BranchRegistrationResponse> registerBranch(@Valid @RequestBody RegisterBranchRequest request,
                                                                     @AuthenticationPrincipal UserDetails principal) {
        BranchRegistrationResponse response = BranchResponseMapper.toRegistrationResponse(
                ownerBranchPortIn.registerBranch(principal.getUsername(), RegisterBranchCommandMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
