package com.technokratos.agona.dto;

import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.Skill;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record VacancyResponse(
        UUID id,
        String title,
        String description,
        String city,
        Integer salaryFrom,
        Integer salaryTo,
        ScheduleType schedule,
        String vacancyLevel,
        Set<Skill> skills,
        Company company,
        List<QuestionResponse> questions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean archived
) {
}
