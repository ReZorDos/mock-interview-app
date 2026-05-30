package com.technokratos.agona.service;

import com.technokratos.agona.dto.ScreeningView;
import com.technokratos.agona.exception.ProgressAlreadyExistException;
import com.technokratos.agona.exception.ProgressNotFoundException;
import com.technokratos.agona.exception.ServiceException;
import com.technokratos.agona.exception.UserNotFoundException;
import com.technokratos.agona.exception.VacancyNotFoundException;
import com.technokratos.agona.model.AudioFile;
import com.technokratos.agona.model.Question;
import com.technokratos.agona.model.User;
import com.technokratos.agona.model.UserProgress;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.AudioFileRepository;
import com.technokratos.agona.repository.UserAnswerRepository;
import com.technokratos.agona.repository.UserProgressRepository;
import com.technokratos.agona.repository.UserRepository;
import com.technokratos.agona.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreeningServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private UserAnswerRepository userAnswerRepository;
    @Mock
    private AudioFileRepository audioFileRepository;
    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AudioStorageService audioStorageService;

    @InjectMocks
    private ScreeningService screeningService;

    private final UUID vacancyId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID progressId = UUID.randomUUID();

    @Test
    void startOrResume_existingUnfinishedProgress_returnsId() {
        UserProgress progress = mock(UserProgress.class);
        when(progress.getFinishedAt()).thenReturn(null);
        when(progress.getId()).thenReturn(progressId);
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.of(progress));

        assertThat(screeningService.startOrResume(vacancyId, userId)).isEqualTo(progressId);
    }

    @Test
    void startOrResume_existingFinishedProgress_throwsProgressAlreadyExistException() {
        UserProgress progress = mock(UserProgress.class);
        when(progress.getFinishedAt()).thenReturn(LocalDateTime.now());
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> screeningService.startOrResume(vacancyId, userId))
                .isInstanceOf(ProgressAlreadyExistException.class);
    }

    @Test
    void startOrResume_noProgress_createsNew() {
        Vacancy vacancy = new Vacancy();
        User user = new User();
        UserProgress saved = UserProgress.builder().build();
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.empty());
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userProgressRepository.save(any(UserProgress.class))).thenReturn(saved);

        screeningService.startOrResume(vacancyId, userId);

        verify(userProgressRepository).save(any(UserProgress.class));
    }

    @Test
    void startOrResume_vacancyNotFound_throwsVacancyNotFoundException() {
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.empty());
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.startOrResume(vacancyId, userId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void startOrResume_vacancyArchived_throwsServiceException() {
        Vacancy vacancy = new Vacancy();
        vacancy.setArchived(true);
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.empty());
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));

        assertThatThrownBy(() -> screeningService.startOrResume(vacancyId, userId))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("архиве");
    }

    @Test
    void startOrResume_userNotFound_throwsUserNotFoundException() {
        Vacancy vacancy = new Vacancy();
        when(userProgressRepository.findByUserIdAndVacancyId(userId, vacancyId))
                .thenReturn(Optional.empty());
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.startOrResume(vacancyId, userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void getCurrentQuestion_indexInBounds_returnsScreeningView() {
        Question q = mock(Question.class);
        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getTitle()).thenReturn("Backend Dev");
        when(vacancy.getQuestions()).thenReturn(List.of(q));

        UserProgress progress = mock(UserProgress.class);
        when(progress.getCurrentQuestionIndex()).thenReturn(0);
        when(progress.getVacancy()).thenReturn(vacancy);

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));

        ScreeningView view = screeningService.getCurrentQuestion(progressId);

        assertThat(view).isNotNull();
        assertThat(view.currentQuestion()).isSameAs(q);
        assertThat(view.totalQuestions()).isEqualTo(1);
    }

    @Test
    void getCurrentQuestion_indexOutOfBounds_throwsServiceException() {
        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getQuestions()).thenReturn(List.of());

        UserProgress progress = mock(UserProgress.class);
        when(progress.getCurrentQuestionIndex()).thenReturn(0);
        when(progress.getVacancy()).thenReturn(vacancy);

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> screeningService.getCurrentQuestion(progressId))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void getCurrentQuestion_progressNotFound_throwsProgressNotFoundException() {
        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.getCurrentQuestion(progressId))
                .isInstanceOf(ProgressNotFoundException.class);
    }

    @Test
    void submitAnswer_notLastQuestion_returnsFalse() {
        Question q1 = mock(Question.class);
        Question q2 = mock(Question.class);
        List<Question> questions = new ArrayList<>(List.of(q1, q2));

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getQuestions()).thenReturn(questions);

        UserProgress progress = UserProgress.builder()
                .vacancy(vacancy)
                .currentQuestionIndex(0)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        AudioFile audioFile = AudioFile.builder().filePath("p").build();

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));
        when(audioStorageService.upload(eq(file), any(UUID.class))).thenReturn("path/rec.webm");
        when(audioFileRepository.save(any())).thenReturn(audioFile);
        when(userAnswerRepository.save(any())).thenReturn(mock(com.technokratos.agona.model.UserAnswer.class));
        when(userProgressRepository.save(progress)).thenReturn(progress);

        boolean result = screeningService.submitAnswer(progressId, file);

        assertThat(result).isFalse();
        assertThat(progress.getCurrentQuestionIndex()).isEqualTo(1);
        assertThat(progress.getFinishedAt()).isNull();
    }

    @Test
    void submitAnswer_lastQuestion_setsFinishedAndReturnsTrue() {
        Question q = mock(Question.class);
        List<Question> questions = new ArrayList<>(List.of(q));

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getQuestions()).thenReturn(questions);

        UserProgress progress = UserProgress.builder()
                .vacancy(vacancy)
                .currentQuestionIndex(0)
                .build();

        MultipartFile file = mock(MultipartFile.class);
        AudioFile audioFile = AudioFile.builder().filePath("p").build();

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));
        when(audioStorageService.upload(eq(file), any(UUID.class))).thenReturn("path/rec.webm");
        when(audioFileRepository.save(any())).thenReturn(audioFile);
        when(userAnswerRepository.save(any())).thenReturn(mock(com.technokratos.agona.model.UserAnswer.class));
        when(userProgressRepository.save(progress)).thenReturn(progress);

        boolean result = screeningService.submitAnswer(progressId, file);

        assertThat(result).isTrue();
        assertThat(progress.getFinishedAt()).isNotNull();
    }

    @Test
    void submitAnswer_allQuestionsAlreadyAnswered_throwsServiceException() {
        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getQuestions()).thenReturn(List.of());

        UserProgress progress = UserProgress.builder()
                .vacancy(vacancy)
                .currentQuestionIndex(0)
                .build();

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));

        assertThatThrownBy(() -> screeningService.submitAnswer(progressId, mock(MultipartFile.class)))
                .isInstanceOf(ServiceException.class);
    }

    @Test
    void submitAnswer_progressNotFound_throwsProgressNotFoundException() {
        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> screeningService.submitAnswer(progressId, mock(MultipartFile.class)))
                .isInstanceOf(ProgressNotFoundException.class);
    }

    @Test
    void forceFinish_notFinished_setsFinishedAt() {
        UserProgress progress = new UserProgress();
        when(userProgressRepository.findById(progressId)).thenReturn(Optional.of(progress));
        when(userProgressRepository.save(progress)).thenReturn(progress);

        screeningService.forceFinish(progressId);

        assertThat(progress.getFinishedAt()).isNotNull();
        verify(userProgressRepository).save(progress);
    }

    @Test
    void forceFinish_alreadyFinished_doesNotSave() {
        UserProgress progress = new UserProgress();
        progress.setFinishedAt(LocalDateTime.now());
        when(userProgressRepository.findById(progressId)).thenReturn(Optional.of(progress));

        screeningService.forceFinish(progressId);

        verify(userProgressRepository, never()).save(any());
    }

    @Test
    void forceFinish_progressNotFound_doesNothing() {
        when(userProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        screeningService.forceFinish(progressId);

        verify(userProgressRepository, never()).save(any());
    }
}
