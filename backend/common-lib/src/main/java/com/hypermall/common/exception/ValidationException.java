package com.hypermall.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class ValidationException extends BaseException {

    private static final String ERROR_CODE = "VALIDATION_ERROR";
    private final Map<String, String> errors;

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
        this.errors = null;
    }

    public ValidationException(String message, Map<String, String> errors) {
        super(message, HttpStatus.BAD_REQUEST, ERROR_CODE);
        this.errors = errors;
    }
}
