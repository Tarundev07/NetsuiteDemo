package com.atomicnorth.hrm.tenant.service.dto.accessgroup;

import com.atomicnorth.hrm.tenant.service.dto.project.ProjectDTO;
import com.atomicnorth.hrm.tenant.service.dto.project.TaskStoryDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class UserTableEntityDTO {

    private Integer username; // Id or Primary Key
    private String usercode;
    private String firstName;
    private String lastName;
    private String role;
    private String primaryemail;
    private String remark;
    private String usergroup;
    private String userstatus;
    private Date dob;
    private Date creationdate;
    private String reportingmanager;
    private Integer hrManager;
    private String hrFullName;
    private Date joiningdate;
    private Date lastworkingdate;
    private String baselocation;
    private String designation;
    private String accountunit;
    private String department;
    private String policygroup;
    private String division;
    private Integer divisionId;
    private Date marriagedate;
    private String lastupdatedby;
    private Date lastmodifieddate;

    private List<TaskStoryDTO> taskMappingList = new ArrayList<TaskStoryDTO>();
    private List<ProjectDTO> projectList = new ArrayList<>();


    public UserTableEntityDTO() {

    }
}
