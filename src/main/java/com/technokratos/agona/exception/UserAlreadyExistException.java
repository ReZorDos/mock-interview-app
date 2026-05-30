package com.technokratos.agona.exception;

public class UserAlreadyExistException extends AlreadyExistException {
    public UserAlreadyExistException(String userName) {
        super(String.format("User with username = %s, already exist", userName));
    }
}
