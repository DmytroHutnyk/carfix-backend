package com.hutnyk.carfix.entity.openingHours;

import com.hutnyk.carfix.entity.branch.BranchEntity;
import com.hutnyk.carfix.openingHours.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "opening_hours")
@Entity
public class OpeningHoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opening_hour_id", nullable = false)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "close_time", nullable = false)
    private LocalTime closeTime;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;
}
