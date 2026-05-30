package com.technokratos.agona.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionRequest {

    @NotBlank(message = "Текст вопроса обязателен")
    @Size(max = 1000, message = "Текст вопроса не должен превышать 1000 символов")
    private String text;

    @Pattern(regexp = "^(EASY|MEDIUM|HARD)$", message = "Недопустимое значение сложности")
    private String difficulty;

    @Size(max = 100, message = "Категория не должна превышать 100 символов")
    private String category;
}
