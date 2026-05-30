package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.ScreeningHistoryView;
import com.technokratos.agona.model.UserProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ScreeningHistoryMapper {

    @Mapping(target = "progressId", source = "id")
    @Mapping(target = "vacancyId", source = "vacancy.id")
    @Mapping(target = "vacancyTitle", source = "vacancy.title")
    @Mapping(target = "decision", source = "recruiterReview.decision")
    @Mapping(target = "comment", source = "recruiterReview.comment")
    @Mapping(target = "reviewedAt", source = "recruiterReview.createdAt")
    ScreeningHistoryView toView(UserProgress progress);
}
