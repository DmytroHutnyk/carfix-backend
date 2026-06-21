package com.hutnyk.carfix.entity.booking;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.entity.branch.BranchEntity;
import com.hutnyk.carfix.entity.carProfile.CarProfileEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "bookings")
@Entity
public class BookingEntity {

    @Id
    @Column(name = "booking_id", nullable = false)
    private UUID id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingStatus status;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;

    @ManyToOne
    @JoinColumn(name = "car_profile_id", nullable = false)
    private CarProfileEntity carProfileEntity;
}
