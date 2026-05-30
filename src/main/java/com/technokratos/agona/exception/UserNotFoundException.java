package com.technokratos.agona.exception;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(UUID userId) {
        super(String.format("Пользователь не найден: %s", userId));
    }
}
