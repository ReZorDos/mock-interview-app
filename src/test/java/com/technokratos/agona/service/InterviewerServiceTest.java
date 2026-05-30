package com.technokratos.agona.service;

import com.technokratos.agona.dto.FeedbackRequest;
import com.technokratos.agona.dto.ScreeningDetailView;
import com.technokratos.agona.dto.UserProfileView;
import com.technokratos.agona.enums.FeedbackDecision;
import com.technokratos.agona.enums.TranscriptionStatus;
import com.technokratos.agona.exception.UserAnswerNotFoundException;
import com.technokratos.agona.exception.UserProgresNotFoundException;
import com.technokratos.agona.model.AudioFile;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.Question;
import com.technokratos.agona.model.RecruiterReview;
import com.technokratos.agona.model.UserAnswer;
import com.technokratos.agona.model.UserProgress;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.CompanyRepository;
import com.technokratos.agona.repository.RecruiterReviewRepository;
import com.technokratos.agona.repository.UserAnswerRepository;
import com.technokratos.agona.repository.UserProgressRepository;
import com.technokratos.agona.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewerServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private UserAnswerRepository userAnswerRepository;
    @Mock
    private RecruiterReviewRepository recruiterReviewRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private TranscriptionService transcriptionService;
    @Mock
    private UserProfileService userProfileService;

    @InjectMocks
    private InterviewerService interviewerService;

    private final UUID interviewerId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID vacancyId = UUID.randomUUID();
    private final UUID progressId = UUID.randomUUID();
    private final UUID answerId = UUID.randomUUID();

    @Test
    void findAllScreenings_delegatesToRepository() {
        List<UserProgress> expected = List.of(mock(UserProgress.class));
        when(userProgressRepository.findAllFinishedByInterviewer(interviewerId)).thenReturn(expected);

        assertThat(interviewerService.findAllScreenings(interviewerId)).isSameAs(expected);
    }

    @Test
    void findCompaniesPaged_delegatesToRepository() {
        Page<Company> page = new PageImpl<>(List.of(mock(Company.class)));
        when(companyRepository.findAllByUser_Id(eq(interviewerId), any(Pageable.class))).thenReturn(page);

        assertThat(interviewerService.findCompaniesPaged(interviewerId, 0)).isSameAs(page);
    }

    @Test
    void countScreeningsForCompany_delegatesToRepository() {
        when(userProgressRepository.countFinishedByCompanyId(companyId)).thenReturn(5L);

        assertThat(interviewerService.countScreeningsForCompany(companyId)).isEqualTo(5L);
    }

    @Test
    void findVacanciesPaged_delegatesToRepository() {
        Page<Vacancy> page = new PageImpl<>(List.of(mock(Vacancy.class)));
        when(vacancyRepository.findPageByCompanyId(eq(companyId), any(Pageable.class))).thenReturn(page);

        assertThat(interviewerService.findVacanciesPaged(companyId, 0)).isSameAs(page);
    }

    @Test
    void countScreeningsForVacancy_delegatesToRepository() {
        when(userProgressRepository.countFinishedByVacancyId(vacancyId)).thenReturn(3L);

        assertThat(interviewerService.countScreeningsForVacancy(vacancyId)).isEqualTo(3L);
    }

    @Test
    void findScreeningsByVacancy_delegatesToRepository() {
        Page<UserProgress> page = new PageImpl<>(List.of(mock(UserProgress.class)));
        when(userProgressRepository.findFinishedByVacancyId(eq(vacancyId), any(Pageable.class))).thenReturn(page);

        assertThat(interviewerService.findScreeningsByVacancy(vacancyId, 0)).isSameAs(page);
    }

    @Test
    void getCompanyName_found_returnsName() {
        Company company = mock(Company.class);
        when(company.getName()).thenReturn("Acme");
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThat(interviewerService.getCompanyName(companyId)).isEqualTo("Acme");
    }

    @Test
    void getCompanyName_notFound_returnsEmptyString() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThat(interviewerService.getCompanyName(companyId)).isEmpty();
    }

    @Test
    void getVacancyTitle_found_returnsTitle() {
        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getTitle()).thenReturn("Java Developer");
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));

        assertThat(interviewerService.getVacancyTitle(vacancyId)).isEqualTo("Java Developer");
    }

    @Test
    void getVacancyTitle_notFound_returnsEmptyString() {
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());

        assertThat(interviewerService.getVacancyTitle(vacancyId)).isEmpty();
    }

    @Test
    void getDetail_withReview_returnsFullView() {
        UUID userId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();

        Question question = mock(Question.class);
        when(question.getId()).thenReturn(questionId);
        when(question.getText()).thenReturn("What is Java?");
        when(question.getCategory()).thenReturn("Java");

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getTitle()).thenReturn("Java Dev");
        when(vacancy.getQuestions()).thenReturn(List.of(question));

        com.technokratos.agona.model.User user = mock(com.technokratos.agona.model.User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Alice");

        UserProgress progress = mock(UserProgress.class);
        when(progress.getId()).thenReturn(progressId);
        when(progress.getVacancy()).thenReturn(vacancy);
        when(progress.getUser()).thenReturn(user);
        when(progress.getStartedAt()).thenReturn(LocalDateTime.now());
        when(progress.getFinishedAt()).thenReturn(LocalDateTime.now());

        RecruiterReview review = RecruiterReview.builder()
                .decision(FeedbackDecision.ACCEPTED)
                .comment("Great candidate")
                .build();

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));
        when(userAnswerRepository.findAllByProgressIdWithDetails(progressId)).thenReturn(List.of());
        when(recruiterReviewRepository.findByProgressId(progressId)).thenReturn(Optional.of(review));
        when(userProfileService.getViewByUserId(userId)).thenReturn(mock(UserProfileView.class));

        ScreeningDetailView detail = interviewerService.getDetail(progressId);

        assertThat(detail.progressId()).isEqualTo(progressId);
        assertThat(detail.vacancyTitle()).isEqualTo("Java Dev");
        assertThat(detail.candidateName()).isEqualTo("Alice");
        assertThat(detail.reviewDecision()).isEqualTo(FeedbackDecision.ACCEPTED);
        assertThat(detail.reviewComment()).isEqualTo("Great candidate");
    }

    @Test
    void getDetail_withoutReview_returnsViewWithNullDecision() {
        UUID userId = UUID.randomUUID();

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getTitle()).thenReturn("Backend Dev");
        when(vacancy.getQuestions()).thenReturn(List.of());

        com.technokratos.agona.model.User user = mock(com.technokratos.agona.model.User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Bob");

        UserProgress progress = mock(UserProgress.class);
        when(progress.getId()).thenReturn(progressId);
        when(progress.getVacancy()).thenReturn(vacancy);
        when(progress.getUser()).thenReturn(user);
        when(progress.getStartedAt()).thenReturn(LocalDateTime.now());
        when(progress.getFinishedAt()).thenReturn(null);

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));
        when(userAnswerRepository.findAllByProgressIdWithDetails(progressId)).thenReturn(List.of());
        when(recruiterReviewRepository.findByProgressId(progressId)).thenReturn(Optional.empty());
        when(userProfileService.getViewByUserId(userId)).thenReturn(mock(UserProfileView.class));

        ScreeningDetailView detail = interviewerService.getDetail(progressId);

        assertThat(detail.reviewDecision()).isNull();
        assertThat(detail.answers()).isEmpty();
    }

    @Test
    void getDetail_withAnswer_buildsAnswerView() {
        UUID userId = UUID.randomUUID();
        UUID questionId = UUID.randomUUID();
        UUID uAnswerId = UUID.randomUUID();

        Question question = mock(Question.class);
        when(question.getId()).thenReturn(questionId);
        when(question.getText()).thenReturn("Describe SOLID");
        when(question.getCategory()).thenReturn("Patterns");

        Vacancy vacancy = mock(Vacancy.class);
        when(vacancy.getTitle()).thenReturn("Senior Dev");
        when(vacancy.getQuestions()).thenReturn(List.of(question));

        com.technokratos.agona.model.User user = mock(com.technokratos.agona.model.User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Charlie");

        UserProgress progress = mock(UserProgress.class);
        when(progress.getId()).thenReturn(progressId);
        when(progress.getVacancy()).thenReturn(vacancy);
        when(progress.getUser()).thenReturn(user);
        when(progress.getStartedAt()).thenReturn(LocalDateTime.now());
        when(progress.getFinishedAt()).thenReturn(LocalDateTime.now());

        UserAnswer answer = mock(UserAnswer.class);
        when(answer.getId()).thenReturn(uAnswerId);
        when(answer.getQuestion()).thenReturn(question);
        when(answer.getAudioFile()).thenReturn(mock(AudioFile.class));
        when(answer.getTranscribedText()).thenReturn("Answer text");
        when(answer.getTranscriptionStatus()).thenReturn(TranscriptionStatus.COMPLETED);

        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.of(progress));
        when(userAnswerRepository.findAllByProgressIdWithDetails(progressId)).thenReturn(List.of(answer));
        when(recruiterReviewRepository.findByProgressId(progressId)).thenReturn(Optional.empty());
        when(userProfileService.getViewByUserId(userId)).thenReturn(mock(UserProfileView.class));

        ScreeningDetailView detail = interviewerService.getDetail(progressId);

        assertThat(detail.answers()).hasSize(1);
        assertThat(detail.answers().get(0).questionText()).isEqualTo("Describe SOLID");
        assertThat(detail.answers().get(0).transcribedText()).isEqualTo("Answer text");
    }

    @Test
    void getDetail_progressNotFound_throwsUserProgresNotFoundException() {
        when(userProgressRepository.findByIdWithQuestions(progressId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewerService.getDetail(progressId))
                .isInstanceOf(UserProgresNotFoundException.class);
    }

    @Test
    void saveFeedback_existingReview_updatesDecisionAndComment() {
        UserProgress progress = mock(UserProgress.class);
        RecruiterReview review = new RecruiterReview();
        FeedbackRequest request = new FeedbackRequest();
        request.setDecision(FeedbackDecision.ACCEPTED);
        request.setComment("Good");

        when(userProgressRepository.findById(progressId)).thenReturn(Optional.of(progress));
        when(recruiterReviewRepository.findByProgressId(progressId)).thenReturn(Optional.of(review));
        when(recruiterReviewRepository.save(review)).thenReturn(review);

        interviewerService.saveFeedback(progressId, request);

        assertThat(review.getDecision()).isEqualTo(FeedbackDecision.ACCEPTED);
        assertThat(review.getComment()).isEqualTo("Good");
        verify(recruiterReviewRepository).save(review);
    }

    @Test
    void saveFeedback_noExistingReview_createsNewReview() {
        UserProgress progress = mock(UserProgress.class);
        FeedbackRequest request = new FeedbackRequest();
        request.setDecision(FeedbackDecision.REJECTED);
        request.setComment("Not a fit");

        when(userProgressRepository.findById(progressId)).thenReturn(Optional.of(progress));
        when(recruiterReviewRepository.findByProgressId(progressId)).thenReturn(Optional.empty());
        when(recruiterReviewRepository.save(any(RecruiterReview.class))).thenAnswer(inv -> inv.getArgument(0));

        interviewerService.saveFeedback(progressId, request);

        verify(recruiterReviewRepository).save(argThat(r ->
                r.getDecision() == FeedbackDecision.REJECTED
                && "Not a fit".equals(r.getComment())
                && r.getProgress() == progress));
    }

    @Test
    void saveFeedback_progressNotFound_throwsUserProgresNotFoundException() {
        when(userProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewerService.saveFeedback(progressId, new FeedbackRequest()))
                .isInstanceOf(UserProgresNotFoundException.class);
    }

    @Test
    void startTranscription_success_setsProcessingStatusAndTranscribes() {
        UserAnswer answer = mock(UserAnswer.class);
        AudioFile audioFile = mock(AudioFile.class);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answer.getAudioFile()).thenReturn(audioFile);
        when(audioFile.getFilePath()).thenReturn("path/file.webm");
        when(transcriptionService.transcribeFromMinioPath("path/file.webm")).thenReturn("text");
        when(userAnswerRepository.save(any())).thenReturn(answer);

        interviewerService.startTranscription(answerId);

        verify(answer).setTranscriptionStatus(TranscriptionStatus.PROCESSING);
        verify(answer).setTranscribedText("text");
        verify(answer).setTranscriptionStatus(TranscriptionStatus.COMPLETED);
        verify(userAnswerRepository, times(2)).save(answer);
    }

    @Test
    void startTranscription_answerNotFound_throwsUserAnswerNotFoundException() {
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewerService.startTranscription(answerId))
                .isInstanceOf(UserAnswerNotFoundException.class);
    }

    @Test
    void doTranscribeAsync_success_setsTextAndCompletedStatus() {
        UserAnswer answer = mock(UserAnswer.class);
        AudioFile audioFile = mock(AudioFile.class);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answer.getAudioFile()).thenReturn(audioFile);
        when(audioFile.getFilePath()).thenReturn("path/file.webm");
        when(transcriptionService.transcribeFromMinioPath("path/file.webm")).thenReturn("result");
        when(userAnswerRepository.save(any())).thenReturn(answer);

        interviewerService.doTranscribeAsync(answerId);

        verify(answer).setTranscribedText("result");
        verify(answer).setTranscriptionStatus(TranscriptionStatus.COMPLETED);
        verify(userAnswerRepository).save(answer);
    }

    @Test
    void doTranscribeAsync_answerNotFound_doesNothing() {
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.empty());

        interviewerService.doTranscribeAsync(answerId);

        verify(userAnswerRepository, never()).save(any());
    }

    @Test
    void doTranscribeAsync_transcriptionFails_setsFailedStatus() {
        UserAnswer answer = mock(UserAnswer.class);
        AudioFile audioFile = mock(AudioFile.class);
        when(userAnswerRepository.findById(answerId)).thenReturn(Optional.of(answer));
        when(answer.getAudioFile()).thenReturn(audioFile);
        when(audioFile.getFilePath()).thenReturn("path/file.webm");
        when(transcriptionService.transcribeFromMinioPath(any())).thenThrow(new RuntimeException("fail"));
        when(userAnswerRepository.save(any())).thenReturn(answer);

        interviewerService.doTranscribeAsync(answerId);

        verify(answer).setTranscriptionStatus(TranscriptionStatus.FAILED);
        verify(answer, never()).setTranscribedText(any());
        verify(userAnswerRepository).save(answer);
    }
}
