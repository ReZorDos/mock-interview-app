package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.AudioDownloadResult;
import com.technokratos.agona.service.UserAnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class AudioController {

    private final UserAnswerService userAnswerService;

    @GetMapping("/audio/{answerId}")
    public ResponseEntity<byte[]> streamAudio(@PathVariable UUID answerId) {
        AudioDownloadResult result = userAnswerService.getAudio(answerId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, result.contentType())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(result.data().length))
                .body(result.data());
    }
}
