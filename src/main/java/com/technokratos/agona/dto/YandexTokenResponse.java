package com.technokratos.agona.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record YandexTokenResponse(
        @JsonProperty("access_token") String accessToken
) {}
