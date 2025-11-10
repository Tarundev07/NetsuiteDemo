package com.atomicnorth.hrm.util;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;


@Data
@Builder
public class OldApiResponseMessage {
    private String message;
    private boolean success;
    private HttpStatus status;
    //private T data;

    public OldApiResponseMessage(String message, boolean success, HttpStatus status) {
        this.message = message;
        this.success = success;
        this.status = status;
    }


}
