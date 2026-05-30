package com.technokratos.agona.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YandexUserInfo(
        String id,
        String login,
        @JsonProperty("default_email") String defaultEmail,
        @JsonProperty("real_name")     String realName
) {}
