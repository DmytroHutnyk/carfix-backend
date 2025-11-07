package com.hutnyk.carfix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,  //temporarily disable security
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
}) public class CarfixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarfixBackendApplication.class, args);
    }

}
