package com.hutnyk.carfix.out.user;

import com.hutnyk.carfix.user.User;

/**
 * Outbound messages to a user. Fire-and-forget: implementations deliver after the current transaction commits
 * and never throw back into the use case.
 */
public interface UserNotificationPortOut {

    void sendEmailVerificationCode(User user, String plainCode);
}
