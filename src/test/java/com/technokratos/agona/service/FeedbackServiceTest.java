package com.technokratos.agona.service;

import com.technokratos.agona.dto.ScreeningHistoryView;
import com.technokratos.agona.mapper.ScreeningHistoryMapper;
import com.technokratos.agona.model.UserProgress;
import com.technokratos.agona.repository.UserProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private UserProgressRepository userProgressRepository;
    @Mock
    private ScreeningHistoryMapper screeningHistoryMapper;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    void findAllScreeningsForUser_returnsMappedPage() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        UserProgress progress = mock(UserProgress.class);
        ScreeningHistoryView view = mock(ScreeningHistoryView.class);

        Page<UserProgress> progressPage = new PageImpl<>(List.of(progress));
        when(userProgressRepository.findAllByUserIdOrderByStartedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(progressPage);
        when(screeningHistoryMapper.toView(progress)).thenReturn(view);

        Page<ScreeningHistoryView> result = feedbackService.findAllScreeningsForUser(userId, pageable);

        assertThat(result.getContent()).containsExactly(view);
    }

    @Test
    void findAllScreeningsForUser_emptyPage_returnsEmptyPage() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        when(userProgressRepository.findAllByUserIdOrderByStartedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<ScreeningHistoryView> result = feedbackService.findAllScreeningsForUser(userId, pageable);

        assertThat(result.getContent()).isEmpty();
    }
}
