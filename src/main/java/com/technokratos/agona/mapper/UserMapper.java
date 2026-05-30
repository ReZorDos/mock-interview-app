package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.RegisterRequest;
import com.technokratos.agona.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = PasswordEncoderMapper.class)
public interface UserMapper {

    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", qualifiedByName = "encodePassword")
    User toEntity(RegisterRequest registerRequest);

}
