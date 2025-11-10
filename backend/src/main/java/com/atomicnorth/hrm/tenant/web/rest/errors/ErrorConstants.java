package com.atomicnorth.hrm.tenant.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String PROBLEM_BASE_URL = "https://www.atomicnorth.tech/problem";
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "/invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "/login-already-used");

    private ErrorConstants() {
    }
}
