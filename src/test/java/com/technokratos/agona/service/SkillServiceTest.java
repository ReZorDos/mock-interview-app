package com.technokratos.agona.service;

import com.technokratos.agona.model.Skill;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.SkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private SkillService skillService;

    @Test
    void updateVacancySkills_nullSkillNames_clearsSkills() {
        Vacancy vacancy = mock(Vacancy.class);
        Set<Skill> skills = new HashSet<>();
        when(vacancy.getSkills()).thenReturn(skills);

        skillService.updateVacancySkills(vacancy, null);

        assertThat(skills).isEmpty();
        verify(skillRepository, never()).save(any());
    }

    @Test
    void updateVacancySkills_emptySkillNames_clearsSkills() {
        Vacancy vacancy = mock(Vacancy.class);
        Set<Skill> skills = new HashSet<>();
        when(vacancy.getSkills()).thenReturn(skills);

        skillService.updateVacancySkills(vacancy, Set.of());

        assertThat(skills).isEmpty();
        verify(skillRepository, never()).save(any());
    }

    @Test
    void updateVacancySkills_existingSkill_doesNotSaveNew() {
        Vacancy vacancy = mock(Vacancy.class);
        Set<Skill> skills = new HashSet<>();
        when(vacancy.getSkills()).thenReturn(skills);

        Skill existingSkill = Skill.builder().name("Java").build();
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(existingSkill));

        skillService.updateVacancySkills(vacancy, Set.of("Java"));

        verify(skillRepository, never()).save(any());
        assertThat(skills).contains(existingSkill);
    }

    @Test
    void updateVacancySkills_newSkill_savesAndAdds() {
        Vacancy vacancy = mock(Vacancy.class);
        Set<Skill> skills = new HashSet<>();
        when(vacancy.getSkills()).thenReturn(skills);

        Skill saved = Skill.builder().name("Kotlin").build();
        when(skillRepository.findByName("Kotlin")).thenReturn(Optional.empty());
        when(skillRepository.save(any(Skill.class))).thenReturn(saved);

        skillService.updateVacancySkills(vacancy, Set.of("Kotlin"));

        verify(skillRepository).save(any(Skill.class));
        assertThat(skills).contains(saved);
    }

    @Test
    void updateVacancySkills_blankAndNullSkillNames_filtered() {
        Vacancy vacancy = mock(Vacancy.class);
        Set<Skill> skills = new HashSet<>();
        when(vacancy.getSkills()).thenReturn(skills);

        Set<String> input = new HashSet<>();
        input.add("  ");
        input.add(null);
        input.add("Java");
        Skill javaSkill = Skill.builder().name("Java").build();
        when(skillRepository.findByName("Java")).thenReturn(Optional.of(javaSkill));

        skillService.updateVacancySkills(vacancy, input);

        assertThat(skills).hasSize(1).contains(javaSkill);
    }

    @Test
    void convertSkillsToString_returnsNames() {
        Skill s1 = Skill.builder().name("Java").build();
        Skill s2 = Skill.builder().name("Spring").build();

        Set<String> result = skillService.convertSkillsToString(Set.of(s1, s2));

        assertThat(result).containsExactlyInAnyOrder("Java", "Spring");
    }

    @Test
    void convertSkillsToString_emptySet_returnsEmpty() {
        Set<String> result = skillService.convertSkillsToString(Set.of());
        assertThat(result).isEmpty();
    }
}
