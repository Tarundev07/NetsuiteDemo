package com.atomicnorth.hrm.tenant.service.dto.message;

import lombok.Data;

import java.util.List;

@Data
public class MessageWrapperDTO {
    private Integer moduleId;
    private Integer moduleFunctionId;
    private List<MessageDTO> messageDTOS;
}
