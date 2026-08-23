package com.hutnyk.carfix.scheduling;

import com.hutnyk.carfix.employee.EmployeeId;
import java.util.List;
import java.util.Set;

public record EmployeeSchedule(EmployeeId employeeId, Set<Integer> roleIds, List<TimeRange> free) {
    public EmployeeSchedule {
        roleIds = Set.copyOf(roleIds);
        free = TimeRanges.union(free);
    }
}
