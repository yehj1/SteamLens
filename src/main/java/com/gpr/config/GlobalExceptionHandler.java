package com.gpr.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "error", "bad_request",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, Object>> handleIo(IOException ex) {
        log.warn("Remote call failed", ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of(
                        "error", "remote_call_failed",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMissingStatic(org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.debug("Static resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "error", "not_found",
                        "message", "Resource not found"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "internal_error",
                        "message", ex.getMessage()
                ));
    }
}
