package com.technokratos.agona.dto;

import com.technokratos.agona.enums.FeedbackDecision;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FeedbackRequest {

    @NotNull(message = "Решение обязательно")
    private FeedbackDecision decision;

    @Size(max = 2000, message = "Комментарий не должен превышать 2000 символов")
    private String comment;
}
