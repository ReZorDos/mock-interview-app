package com.technokratos.agona.exception;

public class ProgressAlreadyExistException extends AlreadyExistException {
    public ProgressAlreadyExistException(String message) {
        super(String.format(message));
    }
}
