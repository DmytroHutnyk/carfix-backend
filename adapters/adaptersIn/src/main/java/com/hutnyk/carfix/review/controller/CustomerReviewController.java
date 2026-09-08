package com.hutnyk.carfix.review.controller;

import com.hutnyk.carfix.in.review.ReviewPortIn;
import com.hutnyk.carfix.review.dto.request.AddReviewRequest;
import com.hutnyk.carfix.review.dto.response.ReviewResponse;
import com.hutnyk.carfix.review.mapper.AddReviewCommandMapper;
import com.hutnyk.carfix.review.mapper.ReviewResponseMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/customer/bookings")
public class CustomerReviewController {

    private final ReviewPortIn reviewPortIn;

    @PostMapping("/{bookingId}/review")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> addReview(@PathVariable(name = "bookingId") UUID bookingId,
                                                    @Valid @RequestBody AddReviewRequest request,
                                                    @AuthenticationPrincipal UserDetails principal) {
        ReviewResponse response = ReviewResponseMapper.toResponse(
                reviewPortIn.addReview(principal.getUsername(), AddReviewCommandMapper.toCommand(bookingId, request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
