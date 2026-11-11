package com.muro_akhaladze.gym_task.exception;

import com.muro_akhaladze.gym_task.dto.error.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(com.muro_akhaladze.gym_task.exceptions.ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiError(com.muro_akhaladze.gym_task.exceptions.ApiException ex) {
        log.warn("API exception caught - code: {}, message: {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage()
        );

        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleFieldValidation(MethodArgumentNotValidException ex) {
        Map<String, String> violations = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError ->
                violations.put(fieldError.getField(), fieldError.getDefaultMessage())
        );

        log.warn("Validation error occurred: {}", violations);

        ErrorResponse response = new ErrorResponse(
                "VALIDATION_FAILURE",
                "Input validation failed",
                violations
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        log.error("Malformed request body: {}", ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                "INVALID_JSON",
                "Unable to parse request body"
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(NoResourceFoundException ex) {
        log.error("Missing endpoint: {}", ex.getResourcePath());

        ErrorResponse response = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                "Requested resource does not exist"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedMethod(HttpRequestMethodNotSupportedException ex) {
        log.error("Invalid HTTP method used: {}", ex.getMethod());

        ErrorResponse response = new ErrorResponse(
                "METHOD_NOT_SUPPORTED",
                "HTTP method not allowed for this endpoint"
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.error("Required query parameter is missing: {}", ex.getParameterName());

        ErrorResponse response = new ErrorResponse(
                "MISSING_PARAMETER",
                "Required query parameter '" + ex.getParameterName() + "' is missing"
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex) {
        log.error("Unexpected error: ", ex);

        ErrorResponse response = new ErrorResponse(
                "SERVER_ERROR",
                "Something went wrong. Please contact support."
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
