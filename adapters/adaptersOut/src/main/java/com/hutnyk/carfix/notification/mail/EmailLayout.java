package com.hutnyk.carfix.notification.mail;

public final class EmailLayout {

    private EmailLayout() {
    }

    public static String wrap(String title, String bodyHtml) {
        return """
                <!doctype html>
                <html lang="en">
                <head><meta charset="utf-8"><title>%s</title></head>
                <body style="margin:0;padding:24px;background:#f4f4f5;font-family:Arial,Helvetica,sans-serif;color:#18181b">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                    <tr><td align="center">
                      <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#ffffff;border-radius:12px;padding:32px">
                        <tr><td style="font-size:22px;font-weight:bold;padding-bottom:16px">CarFix</td></tr>
                        <tr><td style="font-size:15px;line-height:1.5">%s</td></tr>
                        <tr><td style="font-size:12px;color:#71717a;padding-top:24px">This is an automated message from CarFix. Please do not reply to it.</td></tr>
                      </table>
                    </td></tr>
                  </table>
                </body>
                </html>
                """.formatted(Html.escape(title), bodyHtml);
    }
}
