package com.hutnyk.carfix.notification.mapper;

import com.hutnyk.carfix.notification.mail.EmailLayout;
import com.hutnyk.carfix.notification.mail.EmailMessage;
import com.hutnyk.carfix.notification.mail.Html;
import com.hutnyk.carfix.user.EmailVerificationCode;
import com.hutnyk.carfix.user.User;

public final class UserEmailMapper {

    private UserEmailMapper() {
    }

    public static EmailMessage verificationCode(User user, String plainCode) {
        long minutes = EmailVerificationCode.TTL.toMinutes();
        String subject = "Your CarFix verification code: " + plainCode;
        String text = """
                Hi %s,

                Use this code to verify your email address in CarFix:

                    %s

                The code expires in %d minutes. If you did not request it, you can ignore this email.

                CarFix
                """.formatted(user.getName(), plainCode, minutes);
        String html = EmailLayout.wrap("Verify your email", """
                <p>Hi %s,</p>
                <p>Use this code to verify your email address in CarFix:</p>
                <p style="font-size:28px;letter-spacing:6px;font-weight:bold;font-family:monospace">%s</p>
                <p>The code expires in %d minutes. If you did not request it, you can ignore this email.</p>
                """.formatted(Html.escape(user.getName()), Html.escape(plainCode), minutes));
        return new EmailMessage(user.getEmail(), subject, text, html);
    }
}
