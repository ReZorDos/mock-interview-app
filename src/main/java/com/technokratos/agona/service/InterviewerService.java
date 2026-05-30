package com.technokratos.agona.service;

import com.technokratos.agona.dto.FeedbackRequest;
import com.technokratos.agona.dto.ScreeningAnswerView;
import com.technokratos.agona.dto.ScreeningDetailView;
import com.technokratos.agona.dto.UserProfileView;
import com.technokratos.agona.enums.TranscriptionStatus;
import com.technokratos.agona.exception.UserAnswerNotFoundException;
import com.technokratos.agona.exception.UserProgresNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewerService {

    private static final int PAGE_SIZE = 10;

    private final UserProgressRepository userProgressRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final RecruiterReviewRepository recruiterReviewRepository;
    private final CompanyRepository companyRepository;
    private final VacancyRepository vacancyRepository;
    private final TranscriptionService transcriptionService;
    private final UserProfileService userProfileService;

    public List<UserProgress> findAllScreenings(UUID interviewerId) {
        return userProgressRepository.findAllFinishedByInterviewer(interviewerId);
    }

    public Page<Company> findCompaniesPaged(UUID interviewerId, int page) {
        return companyRepository.findAllByUser_Id(interviewerId,
                PageRequest.of(page, PAGE_SIZE, Sort.by("name")));
    }

    public long countScreeningsForCompany(UUID companyId) {
        return userProgressRepository.countFinishedByCompanyId(companyId);
    }

    public Page<Vacancy> findVacanciesPaged(UUID companyId, int page) {
        return vacancyRepository.findPageByCompanyId(companyId,
                PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending()));
    }

    public long countScreeningsForVacancy(UUID vacancyId) {
        return userProgressRepository.countFinishedByVacancyId(vacancyId);
    }

    public Page<UserProgress> findScreeningsByVacancy(UUID vacancyId, int page) {
        return userProgressRepository.findFinishedByVacancyId(vacancyId,
                PageRequest.of(page, PAGE_SIZE));
    }

    public Page<UserProgress> findScreeningsByVacancyWithoutFeedback(UUID vacancyId, int page) {
        return userProgressRepository.findFinishedByVacancyIdWithoutFeedback(vacancyId,
                PageRequest.of(page, PAGE_SIZE));
    }

    public String getCompanyName(UUID companyId) {
        return companyRepository.findById(companyId).map(Company::getName).orElse("");
    }

    public String getVacancyTitle(UUID vacancyId) {
        return vacancyRepository.findById(vacancyId).map(Vacancy::getTitle).orElse("");
    }

    @Transactional(readOnly = true)
    public ScreeningDetailView getDetail(UUID progressId) {
        UserProgress progress = userProgressRepository.findByIdWithQuestions(progressId)
                .orElseThrow(() -> new UserProgresNotFoundException(progressId));

        List<UserAnswer> answers = userAnswerRepository.findAllByProgressIdWithDetails(progressId);
        Map<UUID, UserAnswer> byQuestion = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<ScreeningAnswerView> answerViews = progress.getVacancy().getQuestions().stream()
                .map(q -> buildAnswerView(q, byQuestion.get(q.getId())))
                .toList();

        RecruiterReview review = recruiterReviewRepository.findByProgressId(progressId)
                .orElse(null);
        UserProfileView candidateProfile = userProfileService.getViewByUserId(progress.getUser().getId());

        return new ScreeningDetailView(
                progress.getId(),
                progress.getVacancy().getTitle(),
                progress.getUser().getName(),
                progress.getStartedAt(),
                progress.getFinishedAt(),
                answerViews,
                review != null ? review.getDecision() : null,
                review != null ? review.getComment() : null,
                review != null ? review.getCreatedAt() : null,
                candidateProfile
        );
    }

    @Transactional
    public void saveFeedback(UUID progressId, FeedbackRequest request) {
        UserProgress progress = userProgressRepository.findById(progressId)
                .orElseThrow(() -> new UserProgresNotFoundException(progressId));

        RecruiterReview review = recruiterReviewRepository.findByProgressId(progressId)
                .orElseGet(() -> RecruiterReview.builder()
                        .progress(progress)
                        .build());

        review.setDecision(request.getDecision());
        review.setComment(request.getComment());
        recruiterReviewRepository.save(review);
    }

    @Transactional
    public void startTranscription(UUID answerId) {
        UserAnswer answer = userAnswerRepository.findById(answerId)
                .orElseThrow(() -> new UserAnswerNotFoundException(answerId));
        answer.setTranscriptionStatus(TranscriptionStatus.PROCESSING);
        userAnswerRepository.save(answer);
        doTranscribeAsync(answerId);
    }

    @Async
    @Transactional
    public void doTranscribeAsync(UUID answerId) {
        UserAnswer answer = userAnswerRepository.findById(answerId).orElse(null);
        if (answer == null) return;
        try {
            String text = transcriptionService.transcribeFromMinioPath(answer.getAudioFile().getFilePath());
            answer.setTranscribedText(text);
            answer.setTranscriptionStatus(TranscriptionStatus.COMPLETED);
        } catch (Exception e) {
            log.error("Transcription failed for answer {}: {}", answerId, e.getMessage());
            answer.setTranscriptionStatus(TranscriptionStatus.FAILED);
        }
        userAnswerRepository.save(answer);
    }

    private ScreeningAnswerView buildAnswerView(Question question, UserAnswer answer) {
        StringBuilder audioUrl = new StringBuilder();
        if (answer != null && answer.getAudioFile() != null) {
            audioUrl.append("/audio/").append(answer.getId());
        }
        return ScreeningAnswerView.builder()
                .answerId(answer != null ? answer.getId() : null)
                .questionText(question.getText())
                .category(question.getCategory())
                .transcribedText(answer != null ? answer.getTranscribedText() : null)
                .audioUrl(String.valueOf(audioUrl))
                .transcriptionStatus(answer != null ? answer.getTranscriptionStatus() : null)
                .build();
    }

}
