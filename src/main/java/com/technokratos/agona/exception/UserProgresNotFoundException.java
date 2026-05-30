package com.technokratos.agona.exception;

import java.util.UUID;

public class UserProgresNotFoundException extends NotFoundException {
    public UserProgresNotFoundException(UUID progressId) {
        super(String.format("User progres with id = %s, not found", progressId));
    }
}
