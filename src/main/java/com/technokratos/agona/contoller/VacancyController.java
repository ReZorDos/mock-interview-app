package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.VacancyFilter;
import com.technokratos.agona.dto.VacancyRequest;
import com.technokratos.agona.dto.VacancyResponse;
import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.enums.VacancyLevel;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.CompanyService;
import com.technokratos.agona.service.VacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/vacancy")
@RequiredArgsConstructor
public class VacancyController {

    private final VacancyService vacancyService;
    private final CompanyService companyService;

    @GetMapping
    public String getAll(@ModelAttribute VacancyFilter filter,
                         @RequestParam(defaultValue = "1") int page,
                         Model model) {
        Page<VacancyResponse> vacancyPage = vacancyService.findAllFiltered(filter, page);
        model.addAttribute("vacancies", vacancyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vacancyPage.getTotalPages());
        model.addAttribute("totalElements", vacancyPage.getTotalElements());
        model.addAttribute("filter", filter);
        model.addAttribute("filterQuery", vacancyService.buildFilterQuery(filter));
        model.addAttribute("levels", VacancyLevel.values());
        model.addAttribute("schedules", ScheduleType.values());
        return "vacancies/list";
    }

    @GetMapping("/my")
    public String getMy(@RequestParam(defaultValue = "1") int page,
                        Model model,
                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Page<VacancyResponse> vacancyPage = vacancyService.findAllByUserId(userDetails.getId(), page);
        model.addAttribute("vacancies", vacancyPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vacancyPage.getTotalPages());
        return "vacancies/my-list";
    }

    @GetMapping("/{id}")
    public String getVacancyById(@PathVariable UUID id, Model model) {
        model.addAttribute("vacancy", vacancyService.findById(id));
        return "vacancies/view";
    }

    @GetMapping("/create")
    public String createForm(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("vacancyRequest", new VacancyRequest());
        model.addAttribute("companies", companyService.findAllByUserId(userDetails.getId()));
        model.addAttribute("levels", VacancyLevel.values());
        model.addAttribute("schedules", ScheduleType.values());
        return "vacancies/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("vacancyRequest") VacancyRequest request,
                         BindingResult result,
                         Model model,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("companies", companyService.findAllByUserId(userDetails.getId()));
            model.addAttribute("levels", VacancyLevel.values());
            model.addAttribute("schedules", ScheduleType.values());
            return "vacancies/form";
        }
        VacancyResponse created = vacancyService.create(request, userDetails.getId());
        return "redirect:/vacancy/" + created.id();
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model,
                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("vacancyRequest", vacancyService.findByIdForUpdate(id));
        model.addAttribute("companies", companyService.findAllByUserId(userDetails.getId()));
        model.addAttribute("levels", VacancyLevel.values());
        model.addAttribute("schedules", ScheduleType.values());
        return "vacancies/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                       @Valid @ModelAttribute("vacancyRequest") VacancyRequest request,
                       BindingResult result,
                       Model model,
                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (result.hasErrors()) {
            model.addAttribute("companies", companyService.findAllByUserId(userDetails.getId()));
            model.addAttribute("levels", VacancyLevel.values());
            model.addAttribute("schedules", ScheduleType.values());
            return "vacancies/form";
        }
        vacancyService.update(id, request);
        return "redirect:/vacancy/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        vacancyService.delete(id);
        return "redirect:/vacancy";
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable UUID id) {
        vacancyService.archive(id);
        return "redirect:/vacancy/my";
    }

    @PostMapping("/{id}/unarchive")
    public String unarchive(@PathVariable UUID id) {
        vacancyService.unarchive(id);
        return "redirect:/vacancy/my";
    }
}
