package com.hutnyk.carfix.out.user;

import com.hutnyk.carfix.user.User;

// Delivery starts after commit and never fails the originating use case.
public interface UserNotificationPortOut {

    void sendEmailVerificationCode(User user, String plainCode);
}
