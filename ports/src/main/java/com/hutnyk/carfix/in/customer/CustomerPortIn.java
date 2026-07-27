package com.hutnyk.carfix.in.customer;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.in.customer.commands.RegisterUserCommand;

import java.util.Optional;

public interface CustomerPortIn {
    Customer registerCustomer(RegisterUserCommand command);
    Optional<Customer> loadByCustomerUsername(String email);
}
