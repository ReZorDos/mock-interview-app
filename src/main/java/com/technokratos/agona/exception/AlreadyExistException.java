package com.technokratos.agona.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistException extends ServiceException {
    public AlreadyExistException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
