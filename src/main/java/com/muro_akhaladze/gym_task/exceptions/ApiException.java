package com.muro_akhaladze.gym_task.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public ApiException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
