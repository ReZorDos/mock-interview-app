package com.technokratos.agona.exception;

public class ScreeningAlreadyFinishedException extends BadRequestException {
    public ScreeningAlreadyFinishedException() {
        super("Скрининг уже завершён");
    }
}
