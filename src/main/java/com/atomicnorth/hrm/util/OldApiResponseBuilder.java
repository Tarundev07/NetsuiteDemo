package com.atomicnorth.hrm.util;

import java.util.List;

public class OldApiResponseBuilder {

    public static <T> OldApiResponseMessageBuilder<T> buildSuccessResponse(String responseCode, List<T> data, String responseMessageType) {
        return OldApiResponseMessageBuilder.<T>builder()
                .error(false)  // Use `error` instead of `isError`
                .responseCode(responseCode)
                .tokens(List.of())  // Empty tokens by default
                .data(data)
                .errors(List.of())  // Empty errors by default
                .exceptions(List.of())  // Empty exceptions by default
                .responseMessageType(responseMessageType)
                .build();
    }

    public static OldApiResponseMessageBuilder<?> buildFailureResponse(String responseCode, List<String> errors, List<String> exceptions, String responseMessageType) {
        return OldApiResponseMessageBuilder.builder()
                .error(true)  // Use `error` instead of `isError`
                .responseCode(responseCode)
                .tokens(List.of())  // Empty tokens by default
                .data(null)  // No data in error responses
                .errors(errors)
                .exceptions(exceptions)
                .responseMessageType(responseMessageType)
                .build();
    }

    public static OldApiResponseMessageBuilder<?> buildErrorResponse(Exception ex) {
        return OldApiResponseMessageBuilder.builder()
                .error(true)  // Use `error` instead of `isError`
                .responseCode("INTERNAL_SERVER_ERROR")
                .tokens(List.of())
                .data(null)
                .errors(List.of(ex.getMessage()))
                .exceptions(List.of(ex.getClass().getSimpleName()))
                .responseMessageType("ERROR")
                .build();
    }

    /*-----*/

}
