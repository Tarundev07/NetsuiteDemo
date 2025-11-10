package com.atomicnorth.hrm.tenant.repository.roles;

import com.atomicnorth.hrm.tenant.domain.roles.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface FunctionRepository extends JpaRepository<FunctionEntity, Integer> {
    List<FunctionEntity> findByModuleId(Integer moduleId);

    List<FunctionEntity> findByFunctionId(Integer moduleFeatureId);

    @Query(value = "CALL GetFunctionAndModuleByFeatureId(:moduleFeatureId)", nativeQuery = true)
    List<Object[]> findFunctionIdModuleId(@Param("moduleFeatureId") Integer moduleFeatureId);

    List<FunctionEntity> findAllByFunctionIdIn(List<Integer> functionIds);

    List<FunctionEntity> findByModuleIdAndIsWorkFlowEligible(Integer moduleId, String y);

    FunctionEntity findByShortCode(String code);
}
