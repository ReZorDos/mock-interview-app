package com.technokratos.agona.client;

import com.technokratos.agona.dto.assemblyai.TranscriptRequest;
import java.util.List;
import com.technokratos.agona.dto.assemblyai.TranscriptResponse;
import com.technokratos.agona.dto.assemblyai.UploadResponse;
import com.technokratos.agona.exception.TranscriptionException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AssemblyAiClient {

    private final RestClient assemblyAiRestClient;

    public String uploadAudio(byte[] audioBytes) {
        UploadResponse response = assemblyAiRestClient.post()
                .uri("/v2/upload")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(audioBytes)
                .retrieve()
                .body(UploadResponse.class);

        if (response == null || response.uploadUrl() == null) {
            throw new TranscriptionException("AssemblyAI: не удалось загрузить аудио", HttpStatus.BAD_GATEWAY);
        }
        return response.uploadUrl();
    }

    public String createTranscript(String audioUrl) {
        TranscriptResponse response = assemblyAiRestClient.post()
                .uri("/v2/transcript")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TranscriptRequest(audioUrl, List.of("universal-2")))
                .retrieve()
                .body(TranscriptResponse.class);

        if (response == null || response.id() == null) {
            throw new TranscriptionException("AssemblyAI: не удалось создать транскрипт", HttpStatus.BAD_GATEWAY);
        }
        return response.id();
    }

    public TranscriptResponse getTranscript(String transcriptId) {
        return assemblyAiRestClient.get()
                .uri("/v2/transcript/{id}", transcriptId)
                .retrieve()
                .body(TranscriptResponse.class);
    }
}
