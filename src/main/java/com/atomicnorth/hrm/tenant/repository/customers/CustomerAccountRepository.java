package com.atomicnorth.hrm.tenant.repository.customers;

import com.atomicnorth.hrm.tenant.domain.customers.Customer;
import com.atomicnorth.hrm.tenant.domain.customers.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount,Integer>, JpaSpecificationExecutor<CustomerAccount> {

    Optional<CustomerAccount> findByAccountId(Integer accountId);
}
