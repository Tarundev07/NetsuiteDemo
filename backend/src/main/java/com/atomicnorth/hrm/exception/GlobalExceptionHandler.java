package com.atomicnorth.hrm.exception;

import com.atomicnorth.hrm.util.commonClass.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Handle ResourceNotFoundException
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error("Resource not found: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "NOT_FOUND", "Error", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle MethodArgumentNotValidException (Validation errors)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        }
        //ApiResponse<Map<String, String>> response = new ApiResponse<>(errors, false, "VALIDATION_ERROR", "Warning");
        List<String> errorMessages = new ArrayList<>(errors.values());
        ApiResponse<Map<String, String>> response = new ApiResponse<>(null, false, "VALIDATION_ERROR", "Warning", errorMessages);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle BadApiRequestException
    @ExceptionHandler(BadApiRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadApiRequestException(BadApiRequestException ex) {
        logger.error("Bad API request: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "BAD_REQUEST", "Error", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle InvalidApiRequestException
    @ExceptionHandler(InvalidApiRequestException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidApiRequestException(InvalidApiRequestException ex) {
        logger.error("Invalid API request: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "INVALID_REQUEST", "Error", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle HttpMessageNotReadableException
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        ApiResponse<Object> response = new ApiResponse<>(null, false, "INVALID_JSON", "Error", Collections.singletonList("Invalid JSON format in the request body."));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Illegal argument: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "INVALID_ARGUMENT", "Error", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle DataNotFoundException
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataNotFoundException(DataNotFoundException ex) {
        logger.error("Data not found: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "DATA_NOT_FOUND", "Error", Collections.singletonList(ex.getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Handle DataIntegrityViolationException (Database constraint violations)
  /*  @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        logger.error("Database constraint violation: {}", ex.getMessage());
        ApiResponse<Object> response = new ApiResponse<>(null, false, "DATA_INTEGRITY_VIOLATION", "Error", Collections.singletonList("Database constraint violation: " + ex.getMostSpecificCause().getMessage()));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }*/

    // Handle Generic Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        logger.error("An unexpected error occurred: {}", ex.getMessage());
        if (ex.getMessage().contains("authentication is required")) {
            ApiResponse<Object> response = new ApiResponse<>(null, false, "TOKEN_EXPIRED", "Error", Collections.singletonList("Token expired. Please login to continue."));
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        } else {
            ApiResponse<Object> response = new ApiResponse<>(null, false, "INTERNAL_SERVER_ERROR", "Error", Collections.singletonList("Error occurred while processing the request."));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.ok(
                new ApiResponse<>(null, false, "FILE-SIZE-EXCEEDED", "Error",
                        List.of("File size exceeds the maximum allowed limit of 2MB. Please upload a smaller file.")));
    }
    @ExceptionHandler({
            DataIntegrityViolationException.class,
            org.hibernate.exception.DataException.class,
            TransactionSystemException.class
    })
    public ResponseEntity<ApiResponse<Void>> manageDatabaseExceptions(Exception ex) {
        Throwable rootCause = getRootCause(ex);
        String msg = rootCause != null ? rootCause.getMessage() : ex.getMessage();

        ApiResponse<Void> response;
        if (msg != null && (msg.contains("Data too long") || msg.contains("Data truncation"))) {
            String column = extractColumnName(msg);
            response = new ApiResponse<>(
                    null,
                    false,
                    "DB-DATA-LENGTH-ERROR",
                    "Error",
                    List.of(column != null
                            ? "The value for '" + column + "' exceeds the allowed length."
                            : "One of the fields exceeds the allowed length.")
            );
            return ResponseEntity.badRequest().body(response);
        }
        if (msg != null && msg.toLowerCase().contains("duplicate entry")) {
            String column = extractDuplicateColumn(msg);
            response = new ApiResponse<>(
                    null,
                    false,
                    "DB-DUPLICATE-ERROR",
                    "Error",
                    List.of(column != null
                            ? "Duplicate value not allowed for column '" + column + "'."
                            : "Duplicate value detected in database.")
            );
            return ResponseEntity.badRequest().body(response);
        }
        if (msg != null && msg.toLowerCase().contains("cannot be null")) {
            String column = extractColumnName(msg);
            response = new ApiResponse<>(
                    null,
                    false,
                    "DB-NOTNULL-ERROR",
                    "Error",
                    List.of(column != null
                            ? "Column '" + column + "' cannot be null."
                            : "A required field is missing.")
            );
            return ResponseEntity.badRequest().body(response);
        }
        response = new ApiResponse<>(
                null,
                false,
                "DB-ERROR",
                "Error",
                List.of("A database error occurred: " + msg)
        );
        return ResponseEntity.internalServerError().body(response);
    }
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String extractColumnName(String msg) {
        Matcher matcher = Pattern.compile("for column '(.*?)'").matcher(msg);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractDuplicateColumn(String msg) {
        Matcher matcher = Pattern.compile("for key '(.*?)'").matcher(msg);
        return matcher.find() ? matcher.group(1) : null;
    }
}