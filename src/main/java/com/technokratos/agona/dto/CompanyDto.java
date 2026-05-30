package com.technokratos.agona.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDto {

    private UUID id;

    @NotBlank(message = "Название компании обязательно")
    @Size(max = 100, message = "Название не должно превышать 100 символов")
    private String name;

    @Size(max = 500, message = "Описание не должно превышать 500 символов")
    private String description;

    @Size(max = 255, message = "URL логотипа не должен превышать 255 символов")
    @Pattern(
            regexp = "^$|^https?://.*",
            message = "URL логотипа должен начинаться с http:// или https://"
    )
    private String logoUrl;
}
