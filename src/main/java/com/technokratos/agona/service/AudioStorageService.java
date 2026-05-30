package com.technokratos.agona.service;

import com.technokratos.agona.config.properties.MinioProperties;
import com.technokratos.agona.exception.AudioStorageException;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AudioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public String upload(MultipartFile file, UUID answerId) {
        String objectName = new StringBuilder()
                .append("answers/")
                .append(answerId)
                .append("/")
                .append(file.getOriginalFilename())
                .toString();
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "audio/webm")
                            .build()
            );
            log.info("Аудио загружено в MinIO: {}", objectName);
            return objectName;
        } catch (Exception e) {
            log.error("Ошибка загрузки аудио в MinIO: объект={}", objectName, e);
            throw new AudioStorageException("Ошибка загрузки аудио в хранилище: " + e.getMessage());
        }
    }

    public byte[] download(String objectName) {
        try (var stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getBucket())
                        .object(objectName)
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            log.error("Ошибка скачивания аудио из MinIO: объект={}", objectName, e);
            throw new AudioStorageException("Ошибка скачивания аудио: " + e.getMessage());
        }
    }

}
