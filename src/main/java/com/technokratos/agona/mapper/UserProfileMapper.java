package com.technokratos.agona.mapper;

import com.technokratos.agona.dto.UserProfileForm;
import com.technokratos.agona.model.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserProfileMapper {

    @Mapping(target = "skills", expression = "java(joinSkills(userProfile.getSkills()))")
    UserProfileForm toForm(UserProfile userProfile);

    default String joinSkills(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return String.join(", ", skills);
    }
}
