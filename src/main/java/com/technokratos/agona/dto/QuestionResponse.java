package com.technokratos.agona.dto;

import com.technokratos.agona.enums.Difficulty;
import lombok.Builder;

import java.util.UUID;

@Builder
public record QuestionResponse(
        UUID id,
        String text,
        Difficulty difficulty,
        String category
) {
}
