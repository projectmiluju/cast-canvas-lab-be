package com.castcanvaslab.api.common.global.error;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "Unexpected server error"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_400", "Invalid request input"),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "User not found");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}
