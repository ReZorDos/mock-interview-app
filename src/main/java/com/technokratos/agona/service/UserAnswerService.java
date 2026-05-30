package com.technokratos.agona.service;

import com.technokratos.agona.dto.AudioDownloadResult;
import com.technokratos.agona.dto.UserAnswerResponse;
import com.technokratos.agona.exception.AudioFileNotFoundException;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.exception.UserAnswerNotFoundException;
import com.technokratos.agona.mapper.UserAnswerMapper;
import com.technokratos.agona.model.AudioFile;
import com.technokratos.agona.model.UserAnswer;
import com.technokratos.agona.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final TranscriptionService transcriptionService;
    private final AudioStorageService audioStorageService;
    private final UserAnswerMapper userAnswerMapper;

    public UserAnswerResponse findById(UUID id) {
        UserAnswer answer = userAnswerRepository.findById(id)
                .orElseThrow(() -> new UserAnswerNotFoundException(id));
        return userAnswerMapper.toResponse(answer);
    }

    @Transactional
    public UserAnswerResponse transcribe(UUID id, MultipartFile file) {
        UserAnswer answer = userAnswerRepository.findById(id)
                .orElseThrow(() -> new UserAnswerNotFoundException(id));
        String text = transcriptionService.transcribe(file);
        answer.setTranscribedText(text);
        return userAnswerMapper.toResponse(answer);
    }

    public AudioDownloadResult getAudio(UUID answerId) {
        UserAnswer answer = userAnswerRepository.findById(answerId)
                .orElseThrow(() -> new UserAnswerNotFoundException(answerId));

        AudioFile audioFile = answer.getAudioFile();
        if (audioFile == null) {
            throw new AudioFileNotFoundException(answerId);
        }

        byte[] data = audioStorageService.download(audioFile.getFilePath());
        String contentType = resolveContentType(audioFile.getOriginalFileName());
        return new AudioDownloadResult(data, contentType);
    }

    private String resolveContentType(String filename) {
        if (filename == null) return "audio/webm";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".ogg")) return "audio/ogg";
        if (lower.endsWith(".mp4")) return "audio/mp4";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".wav")) return "audio/wav";
        return "audio/webm";
    }
}
