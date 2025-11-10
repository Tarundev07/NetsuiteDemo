package com.atomicnorth.hrm.tenant.service.profileoptions;

import com.atomicnorth.hrm.tenant.domain.roles.ApplicationModule;
import com.atomicnorth.hrm.tenant.domain.roles.FunctionEntity;
import com.atomicnorth.hrm.tenant.repository.roles.ApplicationModuleRepository;
import com.atomicnorth.hrm.tenant.repository.roles.FunctionRepository;
import com.atomicnorth.hrm.tenant.service.dto.roles.ApplicationModuleDTO;
import com.atomicnorth.hrm.tenant.service.dto.roles.FunctionDTO;
import com.atomicnorth.hrm.tenant.service.translation.SupraTranslationCommonServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfileOptionsService {

    @Autowired
    private ApplicationModuleRepository applicationModuleRepository;

    @Autowired
    private FunctionRepository featureRepository;

    @Autowired
    private SupraTranslationCommonServices translationCommonServices;

    public List<FunctionEntity> getFunctionsByModuleId(Long moduleId) {
        return featureRepository.findByModuleId(Math.toIntExact(moduleId));
    }

    public List<ApplicationModule> getAllModules() {
        return applicationModuleRepository.findAll();
    }

    public List<ApplicationModuleDTO> getModuleFunctionMap() {
        List<ApplicationModule> modules = applicationModuleRepository.findAll();
        List<ApplicationModuleDTO> result = new ArrayList<>();

        for (ApplicationModule module : modules) {
            List<FunctionEntity> functions = featureRepository.findByModuleIdAndIsWorkFlowEligible(
                    module.getModuleId(), "Y"
            );
            if (functions == null || functions.isEmpty()) {
                continue;
            }
            List<FunctionDTO> functionDTOs = functions.stream()
                    .map(f -> new FunctionDTO(
                            f.getFunctionId(),
                            translationCommonServices.getDescription(1, f.getShortCode())
                    ))
                    .collect(Collectors.toList());

            ApplicationModuleDTO moduleDTO = new ApplicationModuleDTO(
                    translationCommonServices.getDescription(1, module.getShortCode()),
                    module.getModuleId(),
                    functionDTOs
            );
            result.add(moduleDTO);
        }
        return result;
    }
}
