package com.hutnyk.carfix.notification.mapper;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.BookingId;
import com.hutnyk.carfix.in.booking.query.BookingServiceView;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.notification.mail.EmailLayout;
import com.hutnyk.carfix.notification.mail.EmailMessage;
import com.hutnyk.carfix.notification.mail.Html;
import com.hutnyk.carfix.user.User;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

public final class BookingEmailMapper {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final String CURRENCY = "PLN";

    private BookingEmailMapper() {
    }

    public static EmailMessage confirmed(User customer, BookingView booking) {
        return build(customer, booking,
                "Booking confirmed",
                "Your booking is confirmed. Here are the details:",
                "You can view or cancel this booking in My Bookings. Cancelling later than "
                        + Booking.SAFE_CANCELLATION_NOTICE.toHours()
                        + " hours before the visit may result in a penalty.");
    }

    public static EmailMessage cancelled(User customer, BookingView booking) {
        return build(customer, booking,
                "Booking cancelled",
                "Your booking has been cancelled. For your records:",
                "If this was a mistake, you can book a new visit any time.");
    }

    private static EmailMessage build(User customer, BookingView b, String heading, String intro, String outro) {
        String subject = heading + " · " + b.branchName() + " · " + DATE.format(b.date()) + " " + TIME.format(b.startTime());
        String text = """
                Hi %s,

                %s

                Reference: %s
                Workshop:  %s
                Address:   %s
                Phone:     %s
                Date:      %s
                Time:      %s
                Vehicle:   %s

                Services:
                %s
                Total: %s

                %s

                CarFix
                """.formatted(
                customer.getName(), intro, reference(b), b.branchName(), address(b), b.branchPhoneNumber(),
                DATE.format(b.date()), timeRange(b), vehicle(b), servicesText(b), price(b.totalPrice()), outro);
        String html = EmailLayout.wrap(heading, """
                <p>Hi %s,</p>
                <p>%s</p>
                <table role="presentation" cellpadding="4" cellspacing="0" style="font-size:15px">
                  <tr><td style="color:#71717a">Reference</td><td><strong>%s</strong></td></tr>
                  <tr><td style="color:#71717a">Workshop</td><td>%s</td></tr>
                  <tr><td style="color:#71717a">Address</td><td>%s</td></tr>
                  <tr><td style="color:#71717a">Phone</td><td>%s</td></tr>
                  <tr><td style="color:#71717a">Date</td><td>%s</td></tr>
                  <tr><td style="color:#71717a">Time</td><td>%s</td></tr>
                  <tr><td style="color:#71717a">Vehicle</td><td>%s</td></tr>
                </table>
                <p><strong>Services</strong></p>
                <ul>%s</ul>
                <p><strong>Total: %s</strong></p>
                <p>%s</p>
                """.formatted(
                Html.escape(customer.getName()), Html.escape(intro), Html.escape(reference(b)),
                Html.escape(b.branchName()), Html.escape(address(b)), Html.escape(b.branchPhoneNumber()),
                DATE.format(b.date()), timeRange(b), Html.escape(vehicle(b)), servicesHtml(b),
                Html.escape(price(b.totalPrice())), Html.escape(outro)));
        return new EmailMessage(customer.getEmail(), subject, text, html);
    }

    private static String reference(BookingView b) {
        return BookingId.of(b.id()).reference();
    }

    private static String address(BookingView b) {
        return b.streetName() + " " + b.buildingNumber() + ", " + b.city();
    }

    private static String timeRange(BookingView b) {
        return TIME.format(b.startTime()) + "–" + TIME.format(b.endTime());
    }

    private static String vehicle(BookingView b) {
        String model = b.brandName() + " " + b.modelName();
        return b.plates() == null
                ? b.carProfileName() + " (" + model + ")"
                : b.carProfileName() + " (" + model + ", " + b.plates() + ")";
    }

    private static String servicesText(BookingView b) {
        return b.services().stream()
                .map(s -> "  - " + s.name() + " — " + price(s.price()))
                .collect(Collectors.joining("\n"));
    }

    private static String servicesHtml(BookingView b) {
        return b.services().stream()
                .map(BookingEmailMapper::serviceItem)
                .collect(Collectors.joining());
    }

    private static String serviceItem(BookingServiceView s) {
        return "<li>" + Html.escape(s.name()) + " — " + Html.escape(price(s.price())) + "</li>";
    }

    private static String price(BigDecimal amount) {
        BigDecimal stripped = amount.stripTrailingZeros();
        String number = stripped.scale() <= 0
                ? stripped.setScale(0, RoundingMode.UNNECESSARY).toPlainString()
                : amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return number + " " + CURRENCY;
    }
}
