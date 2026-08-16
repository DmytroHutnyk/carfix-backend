package com.hutnyk.carfix.branch.dto.response;

import com.hutnyk.carfix.branch.BranchStatus;

import java.util.UUID;

public record BranchRegistrationResponse(UUID id, String name, BranchStatus status) {
}
