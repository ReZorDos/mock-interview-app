package com.technokratos.agona.service;

import com.technokratos.agona.client.AssemblyAiClient;
import com.technokratos.agona.dto.assemblyai.TranscriptResponse;
import com.technokratos.agona.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranscriptionServiceTest {

    @Mock
    private AssemblyAiClient assemblyAiClient;
    @Mock
    private AudioStorageService audioStorageService;

    @InjectMocks
    private TranscriptionService transcriptionService;

    @Test
    void transcribeFromMinioPath_success_returnsText() {
        byte[] audio = "data".getBytes();
        when(audioStorageService.download("path/file.webm")).thenReturn(audio);
        when(assemblyAiClient.uploadAudio(audio)).thenReturn("https://upload-url");
        when(assemblyAiClient.createTranscript("https://upload-url")).thenReturn("tid-1");
        when(assemblyAiClient.getTranscript("tid-1"))
                .thenReturn(new TranscriptResponse("tid-1", "completed", "Hello world", null));

        String result = transcriptionService.transcribeFromMinioPath("path/file.webm");

        assertThat(result).isEqualTo("Hello world");
    }

    @Test
    void transcribe_multipartFile_success_returnsText() throws Exception {
        MockMultipartFile file = new MockMultipartFile("audio", "test.webm", "audio/webm", "data".getBytes());
        when(assemblyAiClient.uploadAudio(any())).thenReturn("https://upload-url");
        when(assemblyAiClient.createTranscript("https://upload-url")).thenReturn("tid-2");
        when(assemblyAiClient.getTranscript("tid-2"))
                .thenReturn(new TranscriptResponse("tid-2", "completed", "Transcript text", null));

        String result = transcriptionService.transcribe(file);

        assertThat(result).isEqualTo("Transcript text");
    }

    @Test
    void transcribe_ioException_throwsServiceException() throws Exception {
        org.springframework.web.multipart.MultipartFile file =
                mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk error"));

        assertThatThrownBy(() -> transcriptionService.transcribe(file))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Не удалось прочитать аудиофайл");
    }

    @Test
    void pollForResult_errorStatus_throwsServiceException() {
        byte[] audio = "data".getBytes();
        when(audioStorageService.download("path")).thenReturn(audio);
        when(assemblyAiClient.uploadAudio(audio)).thenReturn("url");
        when(assemblyAiClient.createTranscript("url")).thenReturn("tid");
        when(assemblyAiClient.getTranscript("tid"))
                .thenReturn(new TranscriptResponse("tid", "error", null, "Bad audio"));

        assertThatThrownBy(() -> transcriptionService.transcribeFromMinioPath("path"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AssemblyAI ошибка транскрипции");
    }

    @Test
    void pollForResult_nullResponse_throwsServiceException() {
        byte[] audio = "data".getBytes();
        when(audioStorageService.download("path")).thenReturn(audio);
        when(assemblyAiClient.uploadAudio(audio)).thenReturn("url");
        when(assemblyAiClient.createTranscript("url")).thenReturn("tid");
        when(assemblyAiClient.getTranscript("tid")).thenReturn(null);

        assertThatThrownBy(() -> transcriptionService.transcribeFromMinioPath("path"))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("AssemblyAI вернул пустой ответ");
    }

    @Test
    void pollForResult_processingThenCompleted_returnsText() {
        byte[] audio = "data".getBytes();
        when(audioStorageService.download("path")).thenReturn(audio);
        when(assemblyAiClient.uploadAudio(audio)).thenReturn("url");
        when(assemblyAiClient.createTranscript("url")).thenReturn("tid");
        when(assemblyAiClient.getTranscript("tid"))
                .thenReturn(new TranscriptResponse("tid", "queued", null, null))
                .thenReturn(new TranscriptResponse("tid", "completed", "Done", null));

        String result = transcriptionService.transcribeFromMinioPath("path");

        assertThat(result).isEqualTo("Done");
        verify(assemblyAiClient, times(2)).getTranscript("tid");
    }
}
