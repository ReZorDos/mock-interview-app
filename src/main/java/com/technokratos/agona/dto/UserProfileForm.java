package com.technokratos.agona.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserProfileForm {

    @Min(value = 14, message = "Возраст должен быть не менее 14 лет")
    @Max(value = 100, message = "Возраст должен быть не более 100 лет")
    private Integer age;

    @Size(max = 500, message = "Образование — не более 500 символов")
    private String education;

    private String experience;

    private String aboutMe;

    private String skills;
}
