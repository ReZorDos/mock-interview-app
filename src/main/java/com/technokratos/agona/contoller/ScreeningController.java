package com.technokratos.agona.contoller;

import com.technokratos.agona.exception.ServiceException;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.ScreeningService;
import com.technokratos.agona.service.VacancyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/screening")
@RequiredArgsConstructor
public class ScreeningController {

    private final ScreeningService screeningService;
    private final VacancyService vacancyService;

    @GetMapping("/instructions/{vacancyId}")
    public String instructions(@PathVariable UUID vacancyId, Model model) {
        model.addAttribute("vacancy", vacancyService.findById(vacancyId));
        return "screening/instructions";
    }

    @GetMapping("/start/{vacancyId}")
    public String start(@PathVariable UUID vacancyId,
                        @AuthenticationPrincipal UserDetailsImpl userDetails,
                        RedirectAttributes redirectAttributes) {
        try {
            UUID progressId = screeningService.startOrResume(vacancyId, userDetails.getId());
            return "redirect:/screening/" + progressId;
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/vacancy/" + vacancyId;
        }
    }

    @GetMapping("/{progressId}")
    public String question(@PathVariable UUID progressId, Model model) {
        model.addAttribute("screening", screeningService.getCurrentQuestion(progressId));
        return "screening/question";
    }

    @PostMapping("/{progressId}/answer")
    public String submitAnswer(@PathVariable UUID progressId,
                               @RequestParam("audio") MultipartFile audio) {
        boolean finished = screeningService.submitAnswer(progressId, audio);
        if (finished) {
            return "redirect:/screening/" + progressId + "/complete";
        }
        return "redirect:/screening/" + progressId;
    }

    @GetMapping("/{progressId}/complete")
    public String complete(@PathVariable UUID progressId, Model model) {
        model.addAttribute("progressId", progressId);
        return "screening/complete";
    }

    @PostMapping("/{progressId}/abandon")
    @ResponseBody
    public ResponseEntity<Void> abandon(@PathVariable UUID progressId) {
        screeningService.forceFinish(progressId);
        return ResponseEntity.ok().build();
    }
}
