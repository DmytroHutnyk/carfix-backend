package com.hutnyk.carfix.out.customer;

import com.hutnyk.carfix.customer.Customer;

import java.util.UUID;

public interface CustomerPortOut {

    Customer insertCustomer(Customer customer);
    Customer loadCustomerByUsername(String email);
    void deleteByUserId(UUID userId);
}
