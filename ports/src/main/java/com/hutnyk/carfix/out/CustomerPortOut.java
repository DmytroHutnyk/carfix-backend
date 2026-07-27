package com.hutnyk.carfix.out;

import com.hutnyk.carfix.customer.Customer;

public interface CustomerPortOut {

    Customer insertCustomer(Customer customer);
    Customer loadCustomerByUsername(String email);
}
