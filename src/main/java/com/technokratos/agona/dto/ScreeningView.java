package com.technokratos.agona.dto;

import com.technokratos.agona.model.Question;
import com.technokratos.agona.model.UserProgress;
import lombok.Builder;

import java.util.UUID;

@Builder
public record ScreeningView(
        UUID progressId,
        String vacancyTitle,
        Question currentQuestion,
        int currentIndex,
        int totalQuestions
) {
    public static ScreeningView of(UserProgress progress, Question question, int total) {
        return ScreeningView.builder()
                .progressId(progress.getId())
                .vacancyTitle(progress.getVacancy().getTitle())
                .currentQuestion(question)
                .currentIndex(progress.getCurrentQuestionIndex())
                .totalQuestions(total)
                .build();
    }
}
