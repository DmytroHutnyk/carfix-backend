package com.hutnyk.carfix.in.branch.commands;

import java.util.List;

public record RegisterBranchEmployeeCommand(String firstName, String lastName, List<String> roles) {
}
