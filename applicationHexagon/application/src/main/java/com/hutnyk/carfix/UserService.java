package com.hutnyk.carfix;

import com.hutnyk.carfix.in.UserPortIn;
import com.hutnyk.carfix.in.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserService implements UserPortIn {

    private final UserPortOut userPortOut;

    @Override
    public Optional<User> loadUserByEmail(String email) {
        return userPortOut.loadUserByEmail(email);
    }

    @Transactional
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
