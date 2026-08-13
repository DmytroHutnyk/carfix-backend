package com.hutnyk.carfix.in.scheduling.query;

import java.util.Set;
import java.util.UUID;

public record EmployeeCandidateView(UUID employeeId, Set<Integer> roleIds) {}
