package com.technokratos.agona.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Ответ кандидата на вопрос скрининга")
@Builder
public record UserAnswerResponse(
        @Schema(description = "UUID ответа")
        UUID id,

        @Schema(description = "UUID вопроса, к которому относится ответ")
        UUID questionId,

        @Schema(description = "Текст расшифровки аудиоответа; null если транскрибация не выполнена")
        String transcribedText,

        @Schema(description = "Дата и время записи ответа")
        LocalDateTime recordedAt
) {}
