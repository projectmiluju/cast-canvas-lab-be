package com.castcanvaslab.api.common.global.error;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {
        Map<String, String> details =
                exception.getBindingResult().getFieldErrors().stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        fieldError ->
                                                fieldError.getDefaultMessage() == null
                                                        ? "Invalid value"
                                                        : fieldError.getDefaultMessage(),
                                        (left, right) -> right,
                                        LinkedHashMap::new));

        return ResponseEntity.status(ErrorCode.INVALID_INPUT.status())
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, details));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException exception) {
        return ResponseEntity.status(exception.errorCode().status())
                .body(ErrorResponse.of(exception.errorCode(), Map.of()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, Map.of()));
    }
}
