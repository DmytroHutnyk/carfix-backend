package com.hutnyk.carfix.user;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.UserPortIn;
import com.hutnyk.carfix.in.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@ApplicationService
@RequiredArgsConstructor
public class UserService implements UserPortIn {

    private final UserPortOut userPortOut;

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userPortOut.loadUserByEmail(email);
    }

    @Override
    public User updateUser(String email, UpdateUserCommand command) {
        User user = userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        if (command.name() != null) {
            user = user.withName(command.name());
        }
        if (command.surname() != null) {
            user = user.withSurname(command.surname());
        }
        if (command.dateOfBirth() != null) {
            user = user.withDateOfBirth(command.dateOfBirth());
        }

        return userPortOut.update(user);
    }
}
