package com.technokratos.agona.exception;

import java.util.UUID;

public class UserAnswerNotFoundException extends NotFoundException {
    public UserAnswerNotFoundException(UUID answerId) {
        super(String.format("User answer with id = %s not found", answerId));
    }
}
