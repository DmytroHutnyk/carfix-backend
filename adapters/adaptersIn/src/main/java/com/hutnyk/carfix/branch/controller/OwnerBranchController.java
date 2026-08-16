package com.hutnyk.carfix.branch.controller;

import com.hutnyk.carfix.branch.dto.response.OwnerBranchSummaryResponse;
import com.hutnyk.carfix.branch.mapper.BranchResponseMapper;
import com.hutnyk.carfix.in.branch.OwnerBranchPortIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
}
