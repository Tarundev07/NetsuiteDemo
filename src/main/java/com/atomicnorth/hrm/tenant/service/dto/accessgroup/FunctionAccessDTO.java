package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
public class FunctionAccessDTO implements Serializable {

    private Long moduleId;
    private String moduleName;
    private Long functionId;
    private String functionName;
    private Long groupId;
    private List<FunctionAccessDTO> functionList;

    public FunctionAccessDTO() {
    }

}