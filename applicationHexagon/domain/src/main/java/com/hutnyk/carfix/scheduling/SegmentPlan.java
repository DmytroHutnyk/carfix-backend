package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.user.UserId;
import java.util.Map;

public record SegmentPlan(Integer serviceId, TimeRange time,
                          Map<Integer, UserId> employeeByRequirementId,
                          Map<Integer, Integer> equipmentByRequirementId) {
    public SegmentPlan {
        employeeByRequirementId = Map.copyOf(employeeByRequirementId);
        equipmentByRequirementId = Map.copyOf(equipmentByRequirementId);
    }
}
