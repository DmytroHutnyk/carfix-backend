package com.hutnyk.carfix.equipment.entity;

import com.hutnyk.carfix.booking.entity.BookingEntity;
import io.hypersistence.utils.hibernate.type.range.PostgreSQLRangeType;
import io.hypersistence.utils.hibernate.type.range.Range;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "equipment_bookings")
@Entity
public class EquipmentBookingEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_booking_id", nullable = false)
    private Integer id;

    @Type(PostgreSQLRangeType.class)
    @Column(name = "booked_time", nullable = false, columnDefinition = "tsrange")
    private Range<LocalDateTime> bookedTime;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipmentEntity;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity bookingEntity;
}
