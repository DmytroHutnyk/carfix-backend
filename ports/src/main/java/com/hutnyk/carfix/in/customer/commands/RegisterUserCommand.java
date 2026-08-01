package com.hutnyk.carfix.in.customer.commands;

public record RegisterUserCommand(
        String name,
        String surname,
        String phoneCountryCode,
        String phoneNumber,
        String email,
        String passwordHash
) {
}
