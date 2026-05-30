package com.technokratos.agona.service;

import com.technokratos.agona.dto.ScreeningHistoryView;
import com.technokratos.agona.enums.FeedbackDecision;
import com.technokratos.agona.mapper.ScreeningHistoryMapper;
import com.technokratos.agona.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final UserProgressRepository userProgressRepository;
    private final ScreeningHistoryMapper screeningHistoryMapper;

    public Page<ScreeningHistoryView> findAllScreeningsForUser(UUID userId, Pageable pageable) {
        return userProgressRepository.findAllByUserIdOrderByStartedAtDesc(userId, pageable)
                .map(screeningHistoryMapper::toView);
    }

    public Page<ScreeningHistoryView> findNoFeedbackScreeningsForUser(UUID userId, Pageable pageable) {
        return userProgressRepository.findFinishedWithoutFeedbackByUserId(userId, pageable)
                .map(screeningHistoryMapper::toView);
    }

    public Page<ScreeningHistoryView> findByDecisionForUser(UUID userId, FeedbackDecision decision, Pageable pageable) {
        return userProgressRepository.findByUserIdAndDecision(userId, decision, pageable)
                .map(screeningHistoryMapper::toView);
    }
}
