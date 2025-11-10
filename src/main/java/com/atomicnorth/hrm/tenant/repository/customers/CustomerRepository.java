package com.atomicnorth.hrm.tenant.repository.customers;

import com.atomicnorth.hrm.tenant.domain.customers.Customer;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerAccount;
import com.atomicnorth.hrm.tenant.service.dto.customers.CustomerNamesDtos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer>, JpaSpecificationExecutor<Customer> {
    Optional<Customer> findByCustomerId(Integer customerId);

    List<CustomerNamesDtos> findAllBy();


}
