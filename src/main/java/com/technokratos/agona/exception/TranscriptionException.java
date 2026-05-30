package com.technokratos.agona.exception;

import org.springframework.http.HttpStatus;

public class TranscriptionException extends ServiceException {
    public TranscriptionException(String message, HttpStatus status) {
        super(message, status);
    }
}
