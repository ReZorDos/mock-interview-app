package com.technokratos.agona.service;

import com.technokratos.agona.dto.AudioDownloadResult;
import com.technokratos.agona.dto.UserAnswerResponse;
import com.technokratos.agona.exception.AudioFileNotFoundException;
import com.technokratos.agona.exception.UserAnswerNotFoundException;
import com.technokratos.agona.mapper.UserAnswerMapper;
import com.technokratos.agona.model.AudioFile;
import com.technokratos.agona.model.UserAnswer;
import com.technokratos.agona.repository.UserAnswerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAnswerServiceTest {

    @Mock
    private UserAnswerRepository userAnswerRepository;
    @Mock
    private TranscriptionService transcriptionService;
    @Mock
    private AudioStorageService audioStorageService;
    @Mock
    private UserAnswerMapper userAnswerMapper;

    @InjectMocks
    private UserAnswerService userAnswerService;

    private final UUID answerId = UUID.randomUUID();

    @Test
    void findById_found_returnsResponse() {
        UserAnswer answer = mock(UserAnswer.class);
        UserAnswerResponse response = mock(UserAnswerResponse.class);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(userAnswerMapper.toResponse(answer)).thenReturn(response);

        assertThat(userAnswerService.findById(answerId)).isSameAs(response);
    }

    @Test
    void findById_notFound_throwsException() {
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAnswerService.findById(answerId))
                .isInstanceOf(UserAnswerNotFoundException.class);
    }

    @Test
    void transcribe_found_setsTextAndReturnsResponse() {
        UserAnswer answer = new UserAnswer();
        UserAnswerResponse response = mock(UserAnswerResponse.class);
        MockMultipartFile file = new MockMultipartFile("audio", "test.webm", "audio/webm", new byte[0]);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(transcriptionService.transcribe(file)).thenReturn("transcribed text");
        when(userAnswerMapper.toResponse(answer)).thenReturn(response);

        UserAnswerResponse result = userAnswerService.transcribe(answerId, file);

        assertThat(answer.getTranscribedText()).isEqualTo("transcribed text");
        assertThat(result).isSameAs(response);
    }

    @Test
    void transcribe_answerNotFound_throwsException() {
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAnswerService.transcribe(answerId,
                new MockMultipartFile("f", new byte[0])))
                .isInstanceOf(UserAnswerNotFoundException.class);
    }

    @Test
    void getAudio_withAudioFile_downloadsAndResolvesContentType() {
        AudioFile audioFile = AudioFile.builder()
                .filePath("answers/1/rec.webm")
                .originalFileName("rec.webm")
                .build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        byte[] data = "bytes".getBytes();
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download("answers/1/rec.webm")).thenReturn(data);

        AudioDownloadResult result = userAnswerService.getAudio(answerId);

        assertThat(result.data()).isEqualTo(data);
        assertThat(result.contentType()).isEqualTo("audio/webm");
    }

    @Test
    void getAudio_nullAudioFile_throwsAudioFileNotFoundException() {
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(null);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));

        assertThatThrownBy(() -> userAnswerService.getAudio(answerId))
                .isInstanceOf(AudioFileNotFoundException.class);
    }

    @Test
    void getAudio_answerNotFound_throwsException() {
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAnswerService.getAudio(answerId))
                .isInstanceOf(UserAnswerNotFoundException.class);
    }

    @Test
    void getAudio_oggFile_returnsOggContentType() {
        AudioFile audioFile = AudioFile.builder()
                .filePath("path/rec.ogg")
                .originalFileName("rec.ogg")
                .build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download(any())).thenReturn(new byte[0]);

        assertThat(userAnswerService.getAudio(answerId).contentType()).isEqualTo("audio/ogg");
    }

    @Test
    void getAudio_mp4File_returnsMp4ContentType() {
        AudioFile audioFile = AudioFile.builder().filePath("p").originalFileName("rec.mp4").build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download(any())).thenReturn(new byte[0]);

        assertThat(userAnswerService.getAudio(answerId).contentType()).isEqualTo("audio/mp4");
    }

    @Test
    void getAudio_mp3File_returnsMpegContentType() {
        AudioFile audioFile = AudioFile.builder().filePath("p").originalFileName("rec.mp3").build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download(any())).thenReturn(new byte[0]);

        assertThat(userAnswerService.getAudio(answerId).contentType()).isEqualTo("audio/mpeg");
    }

    @Test
    void getAudio_wavFile_returnsWavContentType() {
        AudioFile audioFile = AudioFile.builder().filePath("p").originalFileName("rec.wav").build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download(any())).thenReturn(new byte[0]);

        assertThat(userAnswerService.getAudio(answerId).contentType()).isEqualTo("audio/wav");
    }

    @Test
    void getAudio_nullFilename_returnsDefaultContentType() {
        AudioFile audioFile = AudioFile.builder().filePath("p").originalFileName(null).build();
        UserAnswer answer = new UserAnswer();
        answer.setAudioFile(audioFile);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(audioStorageService.download(any())).thenReturn(new byte[0]);

        assertThat(userAnswerService.getAudio(answerId).contentType()).isEqualTo("audio/webm");
    }
}
