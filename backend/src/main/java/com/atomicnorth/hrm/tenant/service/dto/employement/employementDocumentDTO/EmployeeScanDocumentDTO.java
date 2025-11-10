package com.atomicnorth.hrm.tenant.service.dto.employement.employementDocumentDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class EmployeeScanDocumentDTO {


    @JsonProperty("DOC_NAME")
    private String docName;

    @JsonProperty("SERVER_DOC_NAME")
    private String serverDocName;

    @JsonProperty("DESCRIPTION")
    private String description;

    @JsonProperty("REMARK")
    private String remark;

    @JsonProperty("DOC_NUMBER")
    private String docNumber;

    @JsonProperty("DOC_TYPE")
    private String docType;


    @JsonProperty("ACTIVE_FLAGS")
    private List<String> activeFlags; // List of active flags, one for each file

    @JsonProperty("DELETE_FLAGS")
    private List<String> deleteFlags; // List of delete flags, one for each file

    @JsonProperty("USER_NAME")
    private String userName;

    @JsonProperty("FILES")
    private List<MultipartFile> files; // List of files to be uploaded

    // Getters and Setters
    // (Use Lombok @Getter, @Setter annotations if needed)
}

