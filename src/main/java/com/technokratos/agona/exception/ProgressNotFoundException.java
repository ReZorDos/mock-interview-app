package com.technokratos.agona.exception;

import java.util.UUID;

public class ProgressNotFoundException extends NotFoundException {
    public ProgressNotFoundException(UUID progressId) {
        super(String.format("Прогресс не найден: %s",progressId));
    }
}
