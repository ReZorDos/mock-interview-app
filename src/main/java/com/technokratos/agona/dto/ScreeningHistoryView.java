package com.technokratos.agona.dto;

import com.technokratos.agona.enums.FeedbackDecision;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record ScreeningHistoryView(
        UUID progressId,
        UUID vacancyId,
        String vacancyTitle,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        FeedbackDecision decision,
        String comment,
        LocalDateTime reviewedAt
) {
    public boolean inProgress() {
        return finishedAt == null;
    }

    public boolean completed() {
        return finishedAt != null;
    }

    public boolean hasFeedback() {
        return reviewedAt != null;
    }
}
