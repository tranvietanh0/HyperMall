package com.hypermall.ai.exception;

import com.hypermall.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class AiServiceUnavailableException extends BaseException {

    public AiServiceUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE");
    }

    public AiServiceUnavailableException(String message, Throwable cause) {
        super(message, cause, HttpStatus.SERVICE_UNAVAILABLE, "AI_SERVICE_UNAVAILABLE");
    }
}
