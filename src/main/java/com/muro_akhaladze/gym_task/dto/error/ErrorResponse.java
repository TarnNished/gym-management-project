package com.muro_akhaladze.gym_task.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String error;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> fieldErrors;

    public ErrorResponse(String error, String message, Map<String, String> fieldErrors) {
        this.error = error;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = fieldErrors;
        this.message = message;

    }

    public ErrorResponse(String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.fieldErrors = null;
        this.error = error;

    }
}