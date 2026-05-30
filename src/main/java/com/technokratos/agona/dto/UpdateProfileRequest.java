package com.technokratos.agona.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @Size(min = 8, message = "Пароль минимум 8 символов")
    private String newPassword;

}
