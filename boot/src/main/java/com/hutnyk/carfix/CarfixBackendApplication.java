package com.hutnyk.carfix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackageClasses = BaseEntityScan.class)
public class CarfixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarfixBackendApplication.class, args);
    }

}
