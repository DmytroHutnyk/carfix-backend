package com.hutnyk.carfix.booking;

import com.hutnyk.carfix.booking.exception.BookingNotFoundException;
import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.booking.BookingPortIn;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@ApplicationService
@RequiredArgsConstructor
public class BookingService implements BookingPortIn {

    private final CustomerPortOut customerPortOut;
    private final BookingPortOut bookingPortOut;

    @Override
    @Transactional(readOnly = true)
    public List<BookingView> getMyBookings(String customerEmail) {
        Customer customer = customerPortOut.loadCustomerByUsername(customerEmail);
        return bookingPortOut.findAllViewsByCustomerId(customer.getUser().getId().id());
    }

    @Override
    public BookingView cancelBooking(String customerEmail, UUID bookingId) {
        Customer customer = customerPortOut.loadCustomerByUsername(customerEmail);
        UUID customerId = customer.getUser().getId().id();

        Booking booking = bookingPortOut.findByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        bookingPortOut.update(booking.cancel());
        bookingPortOut.freeOccupancy(booking.getId());

        return bookingPortOut.findViewByIdAndCustomerId(bookingId, customerId)
                .orElseThrow(() -> new UnexpectedStateException(
                        "Booking disappeared right after cancel: " + bookingId));
    }
}
