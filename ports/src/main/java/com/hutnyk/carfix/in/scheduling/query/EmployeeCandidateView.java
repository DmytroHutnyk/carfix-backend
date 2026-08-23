package com.hutnyk.carfix.in.scheduling.query;

import com.hutnyk.carfix.employee.EmployeeId;

import java.util.Set;

public record EmployeeCandidateView(EmployeeId employeeId, Set<Integer> roleIds) {}
