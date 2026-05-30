package com.technokratos.agona.config;

import com.technokratos.agona.config.properties.MinioProperties;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
        ensureBucketExists(client);
        return client;
    }

    private void ensureBucketExists(MinioClient client) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build()
            );
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("MinIO bucket '{}' создан", properties.getBucket());
            }
        } catch (Exception e) {
            log.error("Ошибка при инициализации MinIO bucket: {}", e.getMessage());
        }
    }
}
