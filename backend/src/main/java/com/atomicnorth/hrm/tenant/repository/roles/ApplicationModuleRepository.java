package com.atomicnorth.hrm.tenant.repository.roles;


import com.atomicnorth.hrm.tenant.domain.roles.ApplicationModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface ApplicationModuleRepository extends JpaRepository<ApplicationModule, Integer> {
    List<ApplicationModule> findByApplicationId(Integer applicationId);

    List<ApplicationModule> findByModuleId(Integer moduleId);

    ApplicationModule findFirstByModuleId(Integer appModule);

    List<ApplicationModule> findAllByModuleIdIn(List<Integer> moduleIds);
}
