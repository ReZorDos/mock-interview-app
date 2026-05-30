package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.CompanyDto;
import com.technokratos.agona.model.Company;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CompanyMapper {

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", expression = "java(true)")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vacancyList", ignore = true)
    Company toEntity(CompanyDto request);

    CompanyDto toDto(Company company);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "vacancyList", ignore = true)
    void updateEntity(CompanyDto request, @MappingTarget Company company);
}
