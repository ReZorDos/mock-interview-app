package com.technokratos.agona.exception;

import java.util.UUID;

public class UserProfileNotFoundException extends NotFoundException {
    public UserProfileNotFoundException(UUID userId) {
        super(String.format("UserProfile with id = %s not found", userId));
    }
}
