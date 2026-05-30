package com.technokratos.agona.exception;

import java.util.UUID;

public class AudioFileNotFoundException extends NotFoundException {
    public AudioFileNotFoundException(UUID answerId) {
        super(String.format("Аудиофайл не найден для ответа, %s", answerId));
    }
}
