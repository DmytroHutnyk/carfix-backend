package com.hutnyk.carfix.booking.entity;

import com.hutnyk.carfix.booking.BookingStatus;
import com.hutnyk.carfix.branch.entity.BranchEntity;
import com.hutnyk.carfix.carProfile.entity.CarProfileEntity;
import com.hutnyk.carfix.service.entity.ServiceEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bookings")
@Entity
public class BookingEntity {

    @Id
    @EqualsAndHashCode.Include
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

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "car_profile_id", nullable = false)
    private CarProfileEntity carProfileEntity;

    @ToString.Exclude
    @ManyToMany
    @JoinTable(name = "bookings_services",
            joinColumns = @JoinColumn(name = "booking_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id"))
    private Set<ServiceEntity> serviceEntities;
}
