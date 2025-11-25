package com.hutnyk.carfix.carProfile;

import com.hutnyk.carfix.user.Customer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "car_profiles")
public class CarProfile {

    @Id
    @Column(name = "car_profile_id", nullable = false)
    private UUID id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "vin", length = 17)
    private String vin;

    @Column(name = "plates", length = 10)
    private String plates;

    @Column(name = "service_certificate_date")
    private LocalDate serviceCertificateDate;

    @Column(name = "insurance_date")
    private LocalDate insuranceDate;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

//    @Column(name = "file_id")
//    private Integer fileId;

    @ManyToOne
    @JoinColumn(name = "model_generation_id", nullable = false)
    private ModelGeneration modelGeneration;


}
