package com.technokratos.agona.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class SkillsConverter implements AttributeConverter<List<String>, String> {

    @Override
    public String convertToDatabaseColumn(List<String> skills) {
        if (skills == null || skills.isEmpty()) return null;
        return skills.stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    @Override
    public List<String> convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) return new ArrayList<>();
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
