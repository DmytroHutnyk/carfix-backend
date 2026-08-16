package com.hutnyk.carfix.in.user;

import com.hutnyk.carfix.in.user.commands.UpdateUserCommand;
import com.hutnyk.carfix.user.User;

import java.util.Optional;

public interface UserPortIn {

    Optional<User> loadUserByEmail(String email);

    User updateUser(String email, UpdateUserCommand command);

    void requestEmailVerification(String email);

    User verifyEmail(String email, String code);
}
