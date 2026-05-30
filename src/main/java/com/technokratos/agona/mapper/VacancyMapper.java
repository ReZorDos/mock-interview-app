package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.QuestionResponse;
import com.technokratos.agona.dto.VacancyRequest;
import com.technokratos.agona.dto.VacancyResponse;
import com.technokratos.agona.model.Question;
import com.technokratos.agona.model.Vacancy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VacancyMapper {

    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "questions", ignore = true)
    Vacancy toEntity(VacancyRequest request);

    @Mapping(target = "company", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "progresses", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateEntity(VacancyRequest request, @MappingTarget Vacancy vacancy);

    QuestionResponse toResponse(Question question);

    VacancyResponse toResponse(Vacancy vacancy);

    List<VacancyResponse> toResponse(List<Vacancy> vacancies);
}
