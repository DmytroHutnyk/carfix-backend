package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.user.UserId;
import java.util.List;
import java.util.Set;

public record EmployeeSchedule(UserId employeeId, Set<Integer> roleIds, List<TimeRange> free) {
    public EmployeeSchedule {
        roleIds = Set.copyOf(roleIds);
        free = List.copyOf(free);
    }
}
