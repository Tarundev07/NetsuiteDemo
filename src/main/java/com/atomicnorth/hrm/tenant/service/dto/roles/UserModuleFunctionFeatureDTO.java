package com.atomicnorth.hrm.tenant.service.dto.roles;

import lombok.Data;

@Data
public class UserModuleFunctionFeatureDTO {

    private String moduleName;
    private String functionName;
    private String featureName;
    private String moduleUrl;
    private String functionUrl;
    private Integer moduleDisplayNo;
    private Integer functionDisplayNo;

    public UserModuleFunctionFeatureDTO(String moduleName, String functionName, String featureName, String moduleUrl, String functionUrl, Integer moduleDisplayNo, Integer functionDisplayNo) {
        this.moduleName = moduleName;
        this.functionName = functionName;
        this.featureName = featureName;
        this.moduleUrl = moduleUrl;
        this.functionUrl = functionUrl;
        this.moduleDisplayNo = moduleDisplayNo;
        this.functionDisplayNo = functionDisplayNo;
    }
}
