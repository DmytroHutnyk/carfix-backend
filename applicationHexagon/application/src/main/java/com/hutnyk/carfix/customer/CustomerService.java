package com.hutnyk.carfix.customer;

import com.hutnyk.carfix.components.ApplicationService;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.CustomerStatus;
import com.hutnyk.carfix.exceptions.EmailAlreadyTakenException;
import com.hutnyk.carfix.exceptions.PhoneNumberAlreadyTakenException;
import com.hutnyk.carfix.in.CustomerPortIn;
import com.hutnyk.carfix.in.commands.RegisterUserCommand;
import com.hutnyk.carfix.out.CustomerPortOut;
import com.hutnyk.carfix.out.UserPortOut;
import com.hutnyk.carfix.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@ApplicationService
@RequiredArgsConstructor
public class CustomerService implements CustomerPortIn {

    private final UserPortOut userPortOut;
    private final CustomerPortOut customerPortOut;

    @Override
    public Customer registerCustomer(RegisterUserCommand command) {

        UserId userId = UserId.genId();

        PhoneNumber phoneNumber = new PhoneNumber(command.phoneCountryCode(), command.phoneNumber());
        PasswordHash passwordHash = PasswordHash.of(command.passwordHash());


        User user = User.of(
                userId,
                command.name(),
                command.surname(),
                phoneNumber,
                command.email(),
                UserRole.CUSTOMER,
                passwordHash,
                null,
                null
        );

        if (userPortOut.existsByEmail(command.email())) {
            throw new EmailAlreadyTakenException("User with email already exists", command.email());
        }

        if(userPortOut.existsByPhoneNumber(phoneNumber)){
            throw new PhoneNumberAlreadyTakenException("User with such phone number already exists", phoneNumber);
        }
        
        Customer customer = Customer.of(user, CustomerStatus.ACTIVE);
        
        return customerPortOut.insertCustomer(customer);
    }

    @Override
    public Optional<Customer> loadByCustomerUsername(String email){
        return Optional.ofNullable(customerPortOut.loadCustomerByUsername(email));
    }
}

