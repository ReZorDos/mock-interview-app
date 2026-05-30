package com.technokratos.agona.dto.assemblyai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TranscriptRequest(
        @JsonProperty("audio_url") String audioUrl,
        @JsonProperty("speech_models") List<String> speechModels
) {}
