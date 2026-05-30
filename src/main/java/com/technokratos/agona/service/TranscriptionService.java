package com.technokratos.agona.service;

import com.technokratos.agona.client.AssemblyAiClient;
import com.technokratos.agona.dto.assemblyai.TranscriptResponse;
import com.technokratos.agona.exception.TranscriptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptionService {

    private static final int MAX_POLLS = 30;
    private static final long POLL_INTERVAL_MS = 3000;

    private final AssemblyAiClient assemblyAiClient;
    private final AudioStorageService audioStorageService;

    public String transcribeFromMinioPath(String filePath) {
        byte[] bytes = audioStorageService.download(filePath);
        String uploadUrl = assemblyAiClient.uploadAudio(bytes);
        String transcriptId = assemblyAiClient.createTranscript(uploadUrl);
        return pollForResult(transcriptId);
    }

    public String transcribe(MultipartFile audioFile) {
        byte[] bytes;
        try {
            bytes = audioFile.getBytes();
        } catch (IOException e) {
            log.error("Не удалось прочитать аудиофайл: {}", audioFile.getOriginalFilename(), e);
            throw new TranscriptionException("Не удалось прочитать аудиофайл", HttpStatus.BAD_REQUEST);
        }

        String uploadUrl = assemblyAiClient.uploadAudio(bytes);
        log.info("Аудио загружено в AssemblyAI: {}", uploadUrl);

        String transcriptId = assemblyAiClient.createTranscript(uploadUrl);
        log.info("Создан транскрипт с id: {}", transcriptId);

        return pollForResult(transcriptId);
    }

    private String pollForResult(String transcriptId) {
        for (int i = 0; i < MAX_POLLS; i++) {
            TranscriptResponse response = assemblyAiClient.getTranscript(transcriptId);

            if (response == null) {
                throw new TranscriptionException("AssemblyAI вернул пустой ответ", HttpStatus.BAD_GATEWAY);
            }

            switch (response.status()) {
                case "completed" -> {
                    log.info("Транскрипция завершена: {}", transcriptId);
                    return response.text();
                }
                case "error" -> throw new TranscriptionException(
                        "AssemblyAI ошибка транскрипции: " + response.error(), HttpStatus.BAD_GATEWAY
                );
                default -> log.debug("Транскрипция в процессе ({}), попытка {}/{}", response.status(), i + 1, MAX_POLLS);
            }

            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Поток транскрипции прерван для transcriptId={}", transcriptId);
                throw new TranscriptionException("Транскрипция прервана", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        throw new TranscriptionException("Транскрипция превысила время ожидания", HttpStatus.GATEWAY_TIMEOUT);
    }
}
