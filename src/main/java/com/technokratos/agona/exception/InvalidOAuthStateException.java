package com.technokratos.agona.exception;

public class InvalidOAuthStateException extends BadRequestException {
    public InvalidOAuthStateException() {
        super("Невалидное состояние OAuth-авторизации");
    }
}
