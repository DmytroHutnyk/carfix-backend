package com.hutnyk.carfix.in.commands;

import java.time.LocalDate;

public record UpdateUserCommand(
        String name,
        String surname,
        LocalDate dateOfBirth
) {
}
