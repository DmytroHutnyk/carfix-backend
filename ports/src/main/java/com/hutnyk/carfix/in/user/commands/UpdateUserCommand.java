package com.hutnyk.carfix.in.user.commands;

import java.time.LocalDate;

public record UpdateUserCommand(
        String name,
        String surname,
        LocalDate dateOfBirth,
        LocationCommand preferredLocation
) {
}
