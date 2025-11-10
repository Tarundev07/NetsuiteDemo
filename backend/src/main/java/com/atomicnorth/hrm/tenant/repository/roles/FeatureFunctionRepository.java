package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.roles.FeatureFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface FeatureFunctionRepository extends JpaRepository<FeatureFunction, Integer> {

    List<FeatureFunction> findByFeatureFunctionId(Integer moduleFeatureId);

    List<FeatureFunction> findByModuleFeatureId(Integer functionId);

    List<FeatureFunction> findByFeatureFunctionIdIn(List<Integer> featureIds);

    List<FeatureFunction> findAllByModuleFeatureIdIn(List<Integer> functionId);
}
