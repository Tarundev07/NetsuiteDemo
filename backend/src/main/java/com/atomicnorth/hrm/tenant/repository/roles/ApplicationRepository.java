package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.roles.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface ApplicationRepository extends JpaRepository<Application, Integer> {
    List<Application> findAll();

    List<Application> findAllByApplicationIdIn(List<Integer> applicationIds);
}
