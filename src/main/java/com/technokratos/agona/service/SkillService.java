package com.technokratos.agona.service;

import com.technokratos.agona.model.Skill;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    @Transactional
    public void updateVacancySkills(Vacancy vacancy, Set<String> skillNames) {
        vacancy.getSkills().clear();

        if (skillNames != null && !skillNames.isEmpty()) {
            List<Skill> skills = skillNames.stream()
                    .filter(name -> name != null && !name.trim().isEmpty())
                    .map(String::trim)
                    .distinct()
                    .map(name -> skillRepository.findByName(name)
                            .orElseGet(() -> {
                                Skill newSkill = Skill.builder()
                                        .name(name)
                                        .build();
                                return skillRepository.save(newSkill);
                            }))
                    .toList();

            vacancy.getSkills().addAll(skills);
        }
    }

    public Set<String> convertSkillsToString(Set<Skill> skills) {
        return skills.stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());
    }
}