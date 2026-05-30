package com.technokratos.agona.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UserProfileView(
        Integer age,
        String education,
        String experience,
        String aboutMe,
        List<String> skills
) {
    public boolean isEmpty() {
        return age == null
                && (education == null || education.isBlank())
                && (experience == null || experience.isBlank())
                && (aboutMe == null || aboutMe.isBlank())
                && (skills == null || skills.isEmpty());
    }
}
