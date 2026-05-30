package com.technokratos.agona.dto.assemblyai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TranscriptResponse(
        String id,
        String status,
        String text,
        @JsonProperty("error") String error
) {}
