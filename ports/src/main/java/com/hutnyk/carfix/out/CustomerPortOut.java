package com.hutnyk.carfix.out;

import com.hutnyk.carfix.customer.Customer;

public interface CustomerPortOut {

    Customer saveUserAndCustomer(Customer customer);
    Customer loadCustomerByUsername(String email);
}
