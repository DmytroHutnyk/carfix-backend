package com.hutnyk.carfix.entity.openingHours;

import com.hutnyk.carfix.entity.branch.BranchEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "opening_hours_exceptions")
@Entity
public class OpeningHoursExceptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exceptions_id", nullable = false)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_open", nullable = false)
    private Boolean isOpen;

    @Column(name = "reason", length = 300)
    private String reason;

    @ManyToOne
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchEntity branchEntity;
}
