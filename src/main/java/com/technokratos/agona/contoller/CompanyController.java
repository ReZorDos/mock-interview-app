package com.technokratos.agona.contoller;

import com.technokratos.agona.dto.CompanyDto;
import com.technokratos.agona.security.UserDetailsImpl;
import com.technokratos.agona.service.CompanyService;
import com.technokratos.agona.service.VacancyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final VacancyService vacancyService;

    @GetMapping("/browse")
    public String browse(@RequestParam(defaultValue = "") String q, Model model) {
        model.addAttribute("companies", companyService.search(q));
        model.addAttribute("q", q);
        return "companies/browse";
    }

    @GetMapping("/{id}/vacancies")
    public String companyVacancies(@PathVariable UUID id, Model model) {
        CompanyDto company = companyService.findById(id);
        model.addAttribute("company", company);
        model.addAttribute("vacancies", vacancyService.findAllByCompanyId(id));
        return "companies/vacancies";
    }

    @GetMapping
    public String getAll(Model model, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        model.addAttribute("companies", companyService.findAllByUserId(userDetails.getId()));
        return "companies/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("companyRequest", new CompanyDto());
        return "companies/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("companyRequest") CompanyDto request,
                         BindingResult result,
                         @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (result.hasErrors()) {
            return "companies/form";
        }
        companyService.create(request, userDetails.getId());
        return "redirect:/company";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable UUID id, Model model) {
        model.addAttribute("companyRequest", companyService.findByIdForUpdate(id));
        return "companies/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable UUID id,
                       @Valid @ModelAttribute("companyRequest") CompanyDto request,
                       BindingResult result) {
        if (result.hasErrors()) {
            return "companies/form";
        }
        companyService.update(id, request);
        return "redirect:/company";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable UUID id) {
        companyService.delete(id);
        return "redirect:/company";
    }
}
