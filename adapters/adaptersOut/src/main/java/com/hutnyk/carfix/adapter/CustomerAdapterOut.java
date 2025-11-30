package com.hutnyk.carfix.adapter;

import com.hutnyk.carfix.components.PersistenceAdapter;
import com.hutnyk.carfix.customer.Customer;
import com.hutnyk.carfix.mapper.CustomerMapper;
import com.hutnyk.carfix.mapper.UserMapper;
import com.hutnyk.carfix.out.CustomerPortOut;
import com.hutnyk.carfix.entity.user.CustomerEntity;
import com.hutnyk.carfix.entity.user.UserEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@PersistenceAdapter
public class CustomerAdapterOut implements CustomerPortOut {

    private final EntityManager entityManager;

    public Customer saveUserAndCustomer(Customer customer){

        UserEntity userEntity = UserMapper.toEntity(customer.getUser(), null, null);
        CustomerEntity customerEntity = CustomerMapper.toEntity(customer, userEntity, null);

        entityManager.persist(customerEntity);
        entityManager.flush();

        Customer mappedReturnedEntity = CustomerMapper.toDomain(customerEntity, UserMapper.toDomain(customerEntity.getUserEntity()));

        return mappedReturnedEntity;
    }
}
