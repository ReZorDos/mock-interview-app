package com.technokratos.agona.dto;

import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.enums.VacancyLevel;
import com.technokratos.agona.validation.ValidSalaryRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidSalaryRange
public class VacancyRequest {

    private UUID id;

    @NotBlank(message = "Название обязательно")
    @Size(max = 150, message = "Название не должно превышать 150 символов")
    private String title;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @Size(max = 100, message = "Город не должен превышать 100 символов")
    private String city;

    @Min(value = 0, message = "Зарплата не может быть отрицательной")
    private Integer salaryFrom;

    @Min(value = 0, message = "Зарплата не может быть отрицательной")
    private Integer salaryTo;

    private ScheduleType schedule;

    @NotNull(message = "Уровень вакансии обязателен")
    private VacancyLevel vacancyLevel;

    @NotNull(message = "Компания обязательна")
    private UUID companyId;

    @Builder.Default
    private Set<@NotBlank(message = "Название скилла не может быть пустым") String> skills = new HashSet<>();

    private List<QuestionRequest> questions;
}
