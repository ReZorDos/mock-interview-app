package com.technokratos.agona.exception;

public class VacancyArchivedException extends BadRequestException {
    public VacancyArchivedException() {
        super("Вакансия находится в архиве и больше не принимает отклики");
    }
}
