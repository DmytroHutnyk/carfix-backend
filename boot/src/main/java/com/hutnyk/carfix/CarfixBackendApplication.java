package com.hutnyk.carfix;

import com.hutnyk.carfix.entity.BaseEntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,  //temporarily disable security
        org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration.class
})
@EntityScan(basePackageClasses = BaseEntityScan.class)
public class CarfixBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarfixBackendApplication.class, args);
    }

}
