package com.hypermall.common.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {

    private static final String ERROR_CODE = "FORBIDDEN";

    public ForbiddenException(String message) {
        super(message, HttpStatus.FORBIDDEN, ERROR_CODE);
    }
}
