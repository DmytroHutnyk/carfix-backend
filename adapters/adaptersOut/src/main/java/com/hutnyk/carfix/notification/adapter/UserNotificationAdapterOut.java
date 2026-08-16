package com.hutnyk.carfix.notification.adapter;

import com.hutnyk.carfix.components.NotificationAdapter;
import com.hutnyk.carfix.notification.mail.EmailSender;
import com.hutnyk.carfix.notification.mapper.UserEmailMapper;
import com.hutnyk.carfix.out.user.UserNotificationPortOut;
import com.hutnyk.carfix.user.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@NotificationAdapter
public class UserNotificationAdapterOut implements UserNotificationPortOut {

    private final EmailSender emailSender;

    @Override
    public void sendEmailVerificationCode(User user, String plainCode) {
        emailSender.send(UserEmailMapper.verificationCode(user, plainCode));
    }
}
