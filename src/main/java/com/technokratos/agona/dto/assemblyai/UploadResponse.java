package com.technokratos.agona.dto.assemblyai;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UploadResponse(
        @JsonProperty("upload_url") String uploadUrl
) {}
