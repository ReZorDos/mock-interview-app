package com.technokratos.agona.exception;

import org.springframework.http.HttpStatus;

public class AudioStorageException extends ServiceException {
    public AudioStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
