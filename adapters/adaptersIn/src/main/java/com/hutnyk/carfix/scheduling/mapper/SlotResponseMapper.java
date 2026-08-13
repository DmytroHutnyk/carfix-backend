package com.hutnyk.carfix.scheduling.mapper;

import com.hutnyk.carfix.in.scheduling.query.BranchSlotsView;
import com.hutnyk.carfix.scheduling.dto.response.BranchSlotsResponse;
import com.hutnyk.carfix.scheduling.dto.response.DaySlotsResponse;
import com.hutnyk.carfix.scheduling.dto.response.SlotResponse;

public class SlotResponseMapper {

    public static BranchSlotsResponse toResponse(BranchSlotsView view) {
        if (view == null) {
            return null;
        }
        return new BranchSlotsResponse(view.chainable(), view.days().stream()
                .map(day -> new DaySlotsResponse(day.date(), day.slots().stream()
                        .map(slot -> new SlotResponse(slot.startTime(), slot.endTime()))
                        .toList()))
                .toList());
    }
}
