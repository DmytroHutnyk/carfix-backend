package com.hutnyk.carfix.equipment.entity;

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
@Table(name = "equipment_availability")
@Entity
public class EquipmentAvailabilityEntity {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_availability_id", nullable = false)
    private Integer id;

    @Type(PostgreSQLRangeType.class)
    @Column(name = "available_time", nullable = false, columnDefinition = "tsrange")
    private Range<LocalDateTime> availableTime;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "series_id")
    private Integer seriesId;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private EquipmentEntity equipmentEntity;
}
