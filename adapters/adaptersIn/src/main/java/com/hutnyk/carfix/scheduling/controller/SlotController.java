package com.hutnyk.carfix.scheduling.controller;

import com.hutnyk.carfix.in.scheduling.SlotPortIn;
import com.hutnyk.carfix.in.scheduling.query.BranchSlotsQuery;
import com.hutnyk.carfix.scheduling.dto.response.BranchSlotsResponse;
import com.hutnyk.carfix.scheduling.mapper.SlotResponseMapper;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/branches")
public class SlotController {

    private final SlotPortIn slotPortIn;

    @GetMapping("/{branchId}/slots")
    public ResponseEntity<BranchSlotsResponse> getSlots(
            @PathVariable("branchId") UUID branchId,
            @RequestParam("serviceIds") @Size(min = 1, max = 3) List<Integer> serviceIds,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        BranchSlotsQuery query = new BranchSlotsQuery(branchId, serviceIds, from, to);
        return ResponseEntity.ok(SlotResponseMapper.toResponse(slotPortIn.getSlots(query)));
    }
}
