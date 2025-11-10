package com.atomicnorth.hrm.tenant.service.dto.twoFa;

import lombok.Data;

@Data
public class OTPRequest {
    private String username;
    private String otp;
    private String clientId;
}
