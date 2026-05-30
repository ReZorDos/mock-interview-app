package com.technokratos.agona.dto;

import com.technokratos.agona.enums.FeedbackDecision;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record FeedbackView(
        UUID progressId,
        UUID vacancyId,
        String vacancyTitle,
        FeedbackDecision decision,
        String comment,
        LocalDateTime reviewedAt
) {}
