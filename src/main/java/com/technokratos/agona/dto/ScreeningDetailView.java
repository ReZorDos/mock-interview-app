package com.technokratos.agona.dto;

import com.technokratos.agona.enums.FeedbackDecision;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ScreeningDetailView(
        UUID progressId,
        String vacancyTitle,
        String candidateName,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        List<ScreeningAnswerView> answers,
        FeedbackDecision reviewDecision,
        String reviewComment,
        LocalDateTime reviewedAt,
        UserProfileView candidateProfile
) {
    public boolean hasReview() { return reviewDecision != null; }
}
