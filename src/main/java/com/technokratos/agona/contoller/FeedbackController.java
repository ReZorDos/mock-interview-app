package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.ScreeningHistoryView;
import com.technokratos.agona.enums.FeedbackDecision;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private static final int PAGE_SIZE = 10;

    private final FeedbackService feedbackService;

    @GetMapping("/my")
    public String myFeedbacks(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String filter,
            Model model) {

        UUID userId = userDetails.getId();
        PageRequest pageRequest = PageRequest.of(page, PAGE_SIZE);

        Page<ScreeningHistoryView> screenings;
        if ("NO_ANSWER".equals(filter)) {
            screenings = feedbackService.findNoFeedbackScreeningsForUser(userId, pageRequest);
        } else if ("ACCEPTED".equals(filter)) {
            screenings = feedbackService.findByDecisionForUser(userId, FeedbackDecision.ACCEPTED, pageRequest);
        } else if ("REJECTED".equals(filter)) {
            screenings = feedbackService.findByDecisionForUser(userId, FeedbackDecision.REJECTED, pageRequest);
        } else {
            screenings = feedbackService.findAllScreeningsForUser(userId, pageRequest);
        }

        model.addAttribute("screenings", screenings);
        model.addAttribute("currentPage", page);
        model.addAttribute("filter", filter);
        return "user/feedbacks";
    }
}
