package com.technokratos.agona.dto;

import com.technokratos.agona.enums.TranscriptionStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ScreeningAnswerView(
        UUID answerId,
        String questionText,
        String difficulty,
        String category,
        String transcribedText,
        String audioUrl,
        TranscriptionStatus transcriptionStatus
) {
    public boolean hasAudio() { return audioUrl != null; }
    public boolean hasTranscription() { return transcribedText != null && !transcribedText.isBlank(); }
    public boolean isProcessing() { return transcriptionStatus == TranscriptionStatus.PROCESSING; }
    public boolean isFailed() { return transcriptionStatus == TranscriptionStatus.FAILED; }
}
