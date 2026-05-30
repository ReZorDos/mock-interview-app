package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.FeedbackRequest;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.UserProgress;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.InterviewerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/interviewer")
@RequiredArgsConstructor
public class InterviewerController {

    private final InterviewerService interviewerService;

    @GetMapping("/screenings")
    public String screenings(@RequestParam(defaultValue = "0") int page,
                             Model model,
                             @AuthenticationPrincipal UserDetailsImpl userDetails) {
        UUID interviewerId = userDetails.getId();
        Page<Company> companies = interviewerService.findCompaniesPaged(interviewerId, page);
        model.addAttribute("companies", companies);
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", companies.getTotalPages());
        model.addAttribute("interviewerId", interviewerId);
        return "interviewer/screenings";
    }

    @GetMapping("/screenings/company/{companyId}")
    public String screeningVacancies(@PathVariable UUID companyId,
                                     @RequestParam(defaultValue = "0") int page,
                                     Model model) {
        var vacancies = interviewerService.findVacanciesPaged(companyId, page);
        model.addAttribute("vacancies", vacancies);
        model.addAttribute("companyId", companyId);
        model.addAttribute("companyName", interviewerService.getCompanyName(companyId));
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", vacancies.getTotalPages());
        return "interviewer/screening-vacancies";
    }

    @GetMapping("/screenings/company/{companyId}/vacancy/{vacancyId}")
    public String screeningList(@PathVariable UUID companyId,
                                @PathVariable UUID vacancyId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "false") boolean noFeedback,
                                Model model) {
        Page<UserProgress> screenings = noFeedback
                ? interviewerService.findScreeningsByVacancyWithoutFeedback(vacancyId, page)
                : interviewerService.findScreeningsByVacancy(vacancyId, page);
        model.addAttribute("screenings", screenings);
        model.addAttribute("companyId", companyId);
        model.addAttribute("vacancyId", vacancyId);
        model.addAttribute("companyName", interviewerService.getCompanyName(companyId));
        model.addAttribute("vacancyTitle", interviewerService.getVacancyTitle(vacancyId));
        model.addAttribute("currentPage", page + 1);
        model.addAttribute("totalPages", screenings.getTotalPages());
        model.addAttribute("noFeedback", noFeedback);
        return "interviewer/screening-list";
    }

    @GetMapping("/screenings/{progressId}")
    public String detail(@PathVariable UUID progressId, Model model) {
        model.addAttribute("detail", interviewerService.getDetail(progressId));
        model.addAttribute("feedbackForm", new FeedbackRequest());
        return "interviewer/screening-detail";
    }

    @PostMapping("/screenings/{progressId}/feedback")
    public String saveFeedback(@PathVariable UUID progressId,
                               @Valid @ModelAttribute("feedbackForm") FeedbackRequest request,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("detail", interviewerService.getDetail(progressId));
            return "interviewer/screening-detail";
        }
        interviewerService.saveFeedback(progressId, request);
        return "redirect:/interviewer/screenings/" + progressId + "#feedback";
    }

    @PostMapping("/screenings/{progressId}/answers/{answerId}/transcribe")
    public String transcribe(@PathVariable UUID progressId, @PathVariable UUID answerId) {
        interviewerService.startTranscription(answerId);
        return "redirect:/interviewer/screenings/" + progressId;
    }
}
