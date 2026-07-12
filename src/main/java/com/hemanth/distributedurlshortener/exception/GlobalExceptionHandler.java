package com.hemanth.distributedurlshortener.exception;

import com.hemanth.distributedurlshortener.response.ErrorResponse;
import com.hemanth.distributedurlshortener.exception.UrlExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex) {

        logger.warn("Validation failed.");

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.putIfAbsent(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Validation Failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles resource not found exceptions
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

        logger.warn("Resource not found: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(response);
    }
    @ExceptionHandler(UrlExpiredException.class)
    public ResponseEntity<ErrorResponse> handleUrlExpired(
            UrlExpiredException ex) {

        logger.warn("Expired URL accessed: {}", ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.GONE)
                .body(response);
    }

    /**
     * Handles all unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(
            Exception ex) {

        logger.error("Unexpected error occurred.", ex);

        ErrorResponse response = ErrorResponse.builder()
                .success(false)
                .message("Internal Server Error")
                .errors(null)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}