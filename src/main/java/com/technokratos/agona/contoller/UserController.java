package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.RegisterRequest;
import com.technokratos.agona.dto.UpdateProfileRequest;
import com.technokratos.agona.dto.UserProfileForm;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.UserProfileService;
import com.technokratos.agona.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserProfileService userProfileService;

    @GetMapping("/login")
    public String login() {
        return "user/login";
    }

    @GetMapping("/register/user")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterRequest());
        return "user/register";
    }

    @PostMapping("/register/user")
    public String registerUser(@Valid @ModelAttribute("registerForm") RegisterRequest request,
                               BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/register";
        }
        userService.registerUser(request);
        return "redirect:/login";
    }

    @GetMapping("/register/interviewer")
    public String registerInterviewerForm(Model model) {
        model.addAttribute("registerForm", new RegisterRequest());
        return "user/register-interviewer";
    }

    @PostMapping("/register/interviewer")
    public String registerInterviewer(@Valid @ModelAttribute("registerForm") RegisterRequest request,
                                      BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "user/register-interviewer";
        }
        userService.registerInterviewer(request);
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal UserDetailsImpl userDetails, Model model) {
        model.addAttribute("profileForm", userService.buildProfileForm(userDetails.getId()));
        if (userService.isRegularUser(userDetails.getId())) {
            model.addAttribute("questionnaire", userProfileService.getFormByUserId(userDetails.getId()));
        }
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @Valid @ModelAttribute("profileForm") UpdateProfileRequest request,
                                BindingResult bindingResult,
                                Model model) {
        if (bindingResult.hasErrors()) {
            if (userService.isRegularUser(userDetails.getId())) {
                model.addAttribute("questionnaire", userProfileService.getFormByUserId(userDetails.getId()));
            }
            return "user/profile";
        }
        userService.updateProfile(userDetails.getId(), request);
        return "redirect:/profile?success";
    }

    @PostMapping("/profile/questionnaire")
    public String saveQuestionnaire(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                    @Valid @ModelAttribute("questionnaire") UserProfileForm form,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("profileForm", userService.buildProfileForm(userDetails.getId()));
            return "user/profile";
        }
        userProfileService.save(userDetails.getId(), form);
        return "redirect:/profile?questionnaireSaved";
    }
}
