package com.technokratos.agona.exception;

import java.util.UUID;

public class VacancyNotFoundException extends NotFoundException {
    public VacancyNotFoundException(UUID vacancyId) {
        super(String.format("Vacancy with id = %s, not found", vacancyId));
    }
}
