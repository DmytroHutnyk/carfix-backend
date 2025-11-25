package com.hutnyk.carfix;

import com.hutnyk.carfix.dto.request.RegisterCustomerRequest;
import com.hutnyk.carfix.dto.response.RegisterCustomerResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public RegisterCustomerResponse registerCustomer(@Valid @RequestBody RegisterCustomerRequest request){
        return new RegisterCustomerResponse();
    }
}
