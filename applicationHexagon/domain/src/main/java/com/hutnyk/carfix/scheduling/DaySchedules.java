package com.hutnyk.carfix.scheduling;

import java.util.List;

public record DaySchedules(List<BaySchedule> bays,
                           List<EmployeeSchedule> employees,
                           List<EquipmentSchedule> equipment) {
    public DaySchedules {
        bays = List.copyOf(bays);
        employees = List.copyOf(employees);
        equipment = List.copyOf(equipment);
    }
}
