package com.hutnyk.carfix.booking.adapter;

import com.hutnyk.carfix.booking.Booking;
import com.hutnyk.carfix.booking.entity.BookingEntity;
import com.hutnyk.carfix.booking.mapper.BookingMapper;
import com.hutnyk.carfix.booking.repository.BookingRepository;
import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.exception.UnexpectedStateException;
import com.hutnyk.carfix.in.booking.query.BookingView;
import com.hutnyk.carfix.out.booking.BookingPortOut;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@PersistenceAdapter
public class BookingAdapterOut implements BookingPortOut {

    private final BookingRepository bookingRepository;

    @Override
    public List<BookingView> findAllViewsByCustomerId(UUID customerId) {
        return bookingRepository.findAllByCustomerIdWithDetails(customerId).stream()
                .map(BookingMapper::toView)
                .toList();
    }

    @Override
    public Optional<BookingView> findViewByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
                .map(BookingMapper::toView);
    }

    @Override
    public Optional<Booking> findByIdAndCustomerId(UUID bookingId, UUID customerId) {
        return bookingRepository.findByIdAndCustomerIdWithDetails(bookingId, customerId)
                .map(BookingMapper::toDomain);
    }

    @Override
    public Booking update(Booking booking) {
        BookingEntity entity = bookingRepository.findById(booking.getId().id())
                .orElseThrow(() -> new UnexpectedStateException(
                        "Booking row missing on update: " + booking.getId().id()));
        BookingMapper.updateEntity(entity, booking);
        return BookingMapper.toDomain(bookingRepository.save(entity));
    }
}
