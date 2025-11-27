package com.hutnyk.carfix.in;

import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.in.commands.RegisterUserCommand;

public interface CustomerPortIn {
    Customer registerCustomer(RegisterUserCommand command);
}

