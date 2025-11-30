package com.hutnyk.carfix.entity.user;

import com.hutnyk.carfix.entity.address.AddressEntity;
import com.hutnyk.carfix.user.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "users")
@Entity
public class UserEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID id;

    @Column(length = 50, nullable = false)
    private String name;

    @Column(length = 50, nullable = false)
    private String surname;

    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    @Column(name = "ph_country_code", length = 4, nullable = false)
    private String phoneCountryCode;

    @Column(name = "email", length = 30, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @OneToOne
    @JoinColumn(name = "address_id")
    private AddressEntity addressEntity;

    @OneToOne(mappedBy = "userEntity")
    private CustomerEntity customerEntity;
}
