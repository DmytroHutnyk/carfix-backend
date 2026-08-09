package com.hutnyk.carfix.branch.entity;

import com.hutnyk.carfix.address.entity.AddressEntity;
import com.hutnyk.carfix.branch.BranchStatus;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursEntity;
import com.hutnyk.carfix.openingHours.entity.OpeningHoursExceptionEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "branches")
@Entity
public class BranchEntity {

    @Id
    @EqualsAndHashCode.Include
    @Column(name = "branch_id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "phone_number", length = 17, nullable = false)
    private String phoneNumber;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BranchStatus status;

    @Column(name = "tz", nullable = false)
    private String tz;

    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity addressEntity;

    // FK to owners(user_id); becomes a @ManyToOne once OwnerEntity is mapped.
    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @ToString.Exclude
    @OneToMany(mappedBy = "branchEntity")
    private Set<OpeningHoursEntity> openingHoursEntities;

    @ToString.Exclude
    @OneToMany(mappedBy = "branchEntity")
    private Set<OpeningHoursExceptionEntity> openingHoursExceptionEntities;
}
