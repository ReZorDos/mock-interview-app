package com.technokratos.agona.service;

import com.technokratos.agona.config.properties.MinioProperties;
import com.technokratos.agona.exception.ServiceException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioStorageServiceTest {

    @Mock
    private MinioClient minioClient;
    @Mock
    private MinioProperties properties;

    @InjectMocks
    private AudioStorageService audioStorageService;

    @BeforeEach
    void setup() {
        when(properties.getBucket()).thenReturn("test-bucket");
    }

    @Test
    void upload_success_returnsObjectName() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", "data".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String result = audioStorageService.upload(file, UUID.randomUUID());

        assertThat(result).contains("answers/").endsWith("/test.webm");
    }

    @Test
    void upload_nullContentType_usesDefaultAudioWebm() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.webm", null, "data".getBytes());
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String result = audioStorageService.upload(file, UUID.randomUUID());

        assertThat(result).isNotNull();
    }

    @Test
    void upload_minioThrows_wrapsInServiceException() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "audio", "test.webm", "audio/webm", "data".getBytes());
        doThrow(new RuntimeException("minio error"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThatThrownBy(() -> audioStorageService.upload(file, UUID.randomUUID()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Ошибка загрузки аудио");
    }

    @Test
    void download_success_returnsBytes() throws Exception {
        byte[] expected = "audio-data".getBytes();
        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.readAllBytes()).thenReturn(expected);
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(response);

        byte[] result = audioStorageService.download("answers/123/file.webm");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void download_minioThrows_wrapsInServiceException() throws Exception {
        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenThrow(new RuntimeException("not found"));

        assertThatThrownBy(() -> audioStorageService.download("answers/123/file.webm"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Ошибка скачивания аудио");
    }
}
