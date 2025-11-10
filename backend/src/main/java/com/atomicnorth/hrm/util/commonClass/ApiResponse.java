package com.atomicnorth.hrm.util.commonClass;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;
    private boolean isSuccess;
    private String responseCode;
    private String responseType;
    private List<String> errors;

    public ApiResponse(T data, boolean isSuccess, String responseCode, String responseType) {
        this.data = data;
        this.isSuccess = isSuccess;
        this.responseCode = responseCode;
        this.responseType = responseType;
    }

    public ApiResponse(T data, boolean isSuccess, String responseCode, String responseType, List<String> error) {
        this.data = data;
        this.isSuccess = isSuccess;
        this.responseCode = responseCode;
        this.responseType = responseType;
        this.errors = error;
    }

}