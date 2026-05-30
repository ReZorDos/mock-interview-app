package com.technokratos.agona.contoller.api;

import com.technokratos.agona.dto.UserAnswerResponse;
import com.technokratos.agona.service.UserAnswerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Answers", description = "Управление аудиоответами кандидатов")
@SecurityRequirement(name = "cookieAuth")
@RestController
@RequestMapping("/api/v1/answers")
@RequiredArgsConstructor
public class UserAnswerController {

    private final UserAnswerService userAnswerService;

    @Operation(
            summary = "Получить ответ по ID",
            description = "Возвращает данные ответа кандидата, включая статус расшифровки и текст транскрибации"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ответ найден",
                    content = @Content(schema = @Schema(implementation = UserAnswerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ответ не найден", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content)
    })
    @GetMapping("/{id}")
    public UserAnswerResponse getById(
            @Parameter(description = "UUID ответа", required = true)
            @PathVariable UUID id) {
        return userAnswerService.findById(id);
    }

    @Operation(
            summary = "Запустить транскрибацию аудиоответа",
            description = "Принимает аудиофайл, загружает его в AssemblyAI и возвращает обновлённый ответ. " +
                    "Транскрибация выполняется синхронно — ответ содержит итоговый текст."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Транскрибация выполнена успешно",
                    content = @Content(schema = @Schema(implementation = UserAnswerResponse.class))),
            @ApiResponse(responseCode = "404", description = "Ответ не найден", content = @Content),
            @ApiResponse(responseCode = "400", description = "Файл не передан или имеет неверный формат", content = @Content),
            @ApiResponse(responseCode = "401", description = "Не аутентифицирован", content = @Content)
    })
    @PostMapping(value = "/{id}/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserAnswerResponse transcribe(
            @Parameter(description = "UUID ответа", required = true)
            @PathVariable UUID id,
            @Parameter(description = "Аудиофайл (wav, mp3, webm и др.)", required = true)
            @RequestParam("file") MultipartFile file) {
        return userAnswerService.transcribe(id, file);
    }
}
