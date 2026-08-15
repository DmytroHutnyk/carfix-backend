package com.hutnyk.carfix.customer.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.customer.entity.CustomerEntity;
import com.hutnyk.carfix.customer.mapper.CustomerMapper;
import com.hutnyk.carfix.customer.repository.CustomerRepository;
import com.hutnyk.carfix.out.customer.CustomerPortOut;
import com.hutnyk.carfix.user.entity.UserEntity;
import com.hutnyk.carfix.user.mapper.UserMapper;
import com.hutnyk.carfix.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PersistenceAdapter
public class CustomerAdapterOut implements CustomerPortOut {

    private final EntityManager entityManager;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;

    @Override
    public Customer insertCustomer(Customer customer){

        UserEntity userEntity = UserMapper.toEntity(customer.getUser(), null);
        CustomerEntity customerEntity = CustomerMapper.toEntity(customer, userEntity, null);

        entityManager.persist(customerEntity);
        entityManager.flush();

        Customer mappedReturnedEntity = CustomerMapper.toDomain(customerEntity, UserMapper.toDomain(customerEntity.getUserEntity()));

        return mappedReturnedEntity;
    }

    @Override
    public Customer loadCustomerByUsername(String email){
        CustomerEntity customerEntity = customerRepository.getCustomerEntityByUserEntity(userRepository.getUserByEmail(email));
        return CustomerMapper.toDomain(customerEntity, UserMapper.toDomain(customerEntity.getUserEntity()));
    }
}
