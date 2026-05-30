package com.technokratos.agona.service;

import com.technokratos.agona.dto.ScreeningView;
import com.technokratos.agona.enums.CompressionStatus;
import com.technokratos.agona.exception.*;
import com.technokratos.agona.model.*;
import com.technokratos.agona.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScreeningService {

    private final UserProgressRepository userProgressRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final AudioFileRepository audioFileRepository;
    private final VacancyRepository vacancyRepository;
    private final UserRepository userRepository;
    private final AudioStorageService audioStorageService;

    @Transactional
    public UUID startOrResume(UUID vacancyId, UUID userId) {
        return userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId)
                .map(progress -> {
                    if (progress.getFinishedAt() != null) {
                        log.warn("Пользователь {} уже проходил скрининг по вакансии {}", userId, vacancyId);
                        throw new ProgressAlreadyExistException("Вы уже проходили скрининг по этой вакансии");
                    }
                    log.info("Возобновление скрининга {} для пользователя {}", progress.getId(), userId);
                    return progress.getId();
                })
                .orElseGet(() -> createProgress(vacancyId, userId));
    }

    @Transactional(readOnly = true)
    public ScreeningView getCurrentQuestion(UUID progressId) {
        UserProgress progress = loadProgress(progressId);
        List<Question> questions = progress.getVacancy().getQuestions();

        if (progress.getCurrentQuestionIndex() >= questions.size()) {
            throw new ScreeningAlreadyFinishedException();
        }

        Question current = questions.get(progress.getCurrentQuestionIndex());
        return ScreeningView.of(progress, current, questions.size());
    }

    @Transactional
    public boolean submitAnswer(UUID progressId, MultipartFile audioFile) {
        UserProgress progress = loadProgress(progressId);
        List<Question> questions = progress.getVacancy().getQuestions();

        if (progress.getCurrentQuestionIndex() >= questions.size()) {
            throw new ScreeningAlreadyFinishedException();
        }

        Question currentQuestion = questions.get(progress.getCurrentQuestionIndex());

        String filePath = audioStorageService.upload(audioFile, UUID.randomUUID());

        AudioFile audio = audioFileRepository.save(AudioFile.builder()
                .filePath(filePath)
                .originalFileName(audioFile.getOriginalFilename())
                .fileSizeBytes(audioFile.getSize())
                .compressionStatus(CompressionStatus.RAW)
                .build());

        userAnswerRepository.save(UserAnswer.builder()
                .progress(progress)
                .question(currentQuestion)
                .recordedAt(LocalDateTime.now())
                .audioFile(audio)
                .build());

        progress.setCurrentQuestionIndex(progress.getCurrentQuestionIndex() + 1);
        boolean finished = progress.getCurrentQuestionIndex() >= questions.size();
        if (finished) {
            progress.setFinishedAt(LocalDateTime.now());
            log.info("Скрининг {} завершён", progressId);
        }
        userProgressRepository.save(progress);

        return finished;
    }

    private UUID createProgress(UUID vacancyId, UUID userId) {
        Vacancy vacancy = vacancyRepository.findById(vacancyId)
                .orElseThrow(() -> new VacancyNotFoundException(vacancyId));
        if (vacancy.isArchived()) {
            log.warn("Попытка начать скрининг по архивной вакансии {}", vacancyId);
            throw new VacancyArchivedException();
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        log.info("Создание нового скрининга: пользователь={}, вакансия={}", userId, vacancyId);
        return userProgressRepository.save(UserProgress.builder()
                .user(user)
                .vacancy(vacancy)
                .startedAt(LocalDateTime.now())
                .currentQuestionIndex(0)
                .build()).getId();
    }

    @Transactional
    public void forceFinish(UUID progressId) {
        userProgressRepository.findById(progressId).ifPresent(progress -> {
            if (progress.getFinishedAt() == null) {
                progress.setFinishedAt(LocalDateTime.now());
                userProgressRepository.save(progress);
            }
        });
    }

    private UserProgress loadProgress(UUID progressId) {
        return userProgressRepository.findByIdWithQuestions(progressId)
                .orElseThrow(() -> new ProgressNotFoundException(progressId));
    }
}
