package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.UserAnswerResponse;
import com.technokratos.agona.model.UserAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserAnswerMapper {

    @Mapping(target = "questionId", source = "question.id")
    UserAnswerResponse toResponse(UserAnswer answer);
}
