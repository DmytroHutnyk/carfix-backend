package com.hutnyk.carfix.branch.controller;

import com.hutnyk.carfix.branch.dto.response.BranchResponse;
import com.hutnyk.carfix.branch.dto.response.BranchReviewsPageResponse;
import com.hutnyk.carfix.branch.mapper.BranchResponseMapper;
import com.hutnyk.carfix.in.branch.BranchPortIn;
import com.hutnyk.carfix.in.branch.query.BranchReviewsQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchPortIn branchPortIn;

    @GetMapping("/{branchId}")
    public ResponseEntity<BranchResponse> getBranch(@PathVariable("branchId") UUID branchId) {
        return ResponseEntity.ok(
                BranchResponseMapper.toResponse(branchPortIn.getBranch(branchId)));
    }

    @GetMapping("/{branchId}/reviews")
    public ResponseEntity<BranchReviewsPageResponse> getReviews(
            @PathVariable("branchId") UUID branchId,
            @RequestParam(name = "sort", required = false) @Size(max = 20) String sort,
            @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) @Max(50) int size) {
        BranchReviewsQuery query = new BranchReviewsQuery(branchId, sort, page, size);
        return ResponseEntity.ok(
                BranchResponseMapper.toResponse(branchPortIn.getReviews(query)));
    }
}
