package com.hutnyk.carfix.user;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.in.user.UserPortIn;
import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.out.user.UserPortOut;
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
        User existing = userPortOut.loadUserByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + email));

        User user = User.of(
                existing.getId(),
                command.name(),
                command.surname(),
                existing.getPhoneNumber(),
                existing.getEmail(),
                existing.getRole(),
                existing.getPasswordHash(),
                command.dateOfBirth(),
                existing.getAddressId()
        );

        return userPortOut.update(user);
    }
}
