package com.technokratos.agona.service;

import com.technokratos.agona.dto.QuestionRequest;
import com.technokratos.agona.dto.VacancyFilter;
import com.technokratos.agona.dto.VacancyRequest;
import com.technokratos.agona.dto.VacancyResponse;
import com.technokratos.agona.enums.VacancyLevel;
import com.technokratos.agona.exception.CompanyNotFoundException;
import com.technokratos.agona.exception.VacancyNotFoundException;
import com.technokratos.agona.mapper.VacancyMapper;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.CompanyRepository;
import com.technokratos.agona.repository.UserRepository;
import com.technokratos.agona.repository.VacancyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacancyServiceTest {

    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private VacancyMapper vacancyMapper;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private SkillService skillService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VacancyService vacancyService;

    private final UUID vacancyId = UUID.randomUUID();
    private final UUID companyId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void findAll_delegatesToRepositoryAndMapper() {
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        when(vacancyRepository.findAllWithAll()).thenReturn(List.of(v));
        when(vacancyMapper.toResponse(List.of(v))).thenReturn(List.of(resp));

        assertThat(vacancyService.findAll()).containsExactly(resp);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAllFiltered_returnsMappedPage() {
        VacancyFilter filter = new VacancyFilter();
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        Page<Vacancy> page = new PageImpl<>(List.of(v));

        when(vacancyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(vacancyMapper.toResponse(v)).thenReturn(resp);

        Page<VacancyResponse> result = vacancyService.findAllFiltered(filter, 1);

        assertThat(result.getContent()).containsExactly(resp);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findAllByUserId_paginated_returnsMappedPage() {
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        Page<Vacancy> page = new PageImpl<>(List.of(v));

        when(vacancyRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(vacancyMapper.toResponse(v)).thenReturn(resp);

        Page<VacancyResponse> result = vacancyService.findAllByUserId(userId, 1);

        assertThat(result.getContent()).containsExactly(resp);
    }

    @Test
    void findAllByUserId_list_delegatesToRepositoryAndMapper() {
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        when(vacancyRepository.findAllByUserId(userId)).thenReturn(List.of(v));
        when(vacancyMapper.toResponse(List.of(v))).thenReturn(List.of(resp));

        assertThat(vacancyService.findAllByUserId(userId)).containsExactly(resp);
    }

    @Test
    void findAllByCompanyId_delegatesToRepositoryAndMapper() {
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        when(vacancyRepository.findAllByCompanyId(companyId)).thenReturn(List.of(v));
        when(vacancyMapper.toResponse(List.of(v))).thenReturn(List.of(resp));

        assertThat(vacancyService.findAllByCompanyId(companyId)).containsExactly(resp);
    }

    @Test
    void findById_found_returnsResponse() {
        Vacancy v = mock(Vacancy.class);
        VacancyResponse resp = mock(VacancyResponse.class);
        when(vacancyRepository.findByIdWithAll(vacancyId)).thenReturn(Optional.of(v));
        when(vacancyMapper.toResponse(v)).thenReturn(resp);

        assertThat(vacancyService.findById(vacancyId)).isSameAs(resp);
    }

    @Test
    void findById_notFound_throwsVacancyNotFoundException() {
        when(vacancyRepository.findByIdWithAll(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.findById(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void create_companyFoundUserFound_savesAndReturnsResponse() {
        VacancyRequest request = VacancyRequest.builder()
                .companyId(companyId)
                .skills(Set.of("Java"))
                .questions(List.of())
                .build();
        Company company = mock(Company.class);
        Vacancy vacancy = new Vacancy();
        VacancyResponse resp = mock(VacancyResponse.class);

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(vacancyMapper.toEntity(request)).thenReturn(vacancy);
        when(userRepository.findById(userId)).thenReturn(Optional.of(new com.technokratos.agona.model.User()));
        when(vacancyRepository.save(any(Vacancy.class))).thenReturn(vacancy);
        when(vacancyMapper.toResponse(vacancy)).thenReturn(resp);

        assertThat(vacancyService.create(request, userId)).isSameAs(resp);
        verify(skillService).updateVacancySkills(eq(vacancy), eq(Set.of("Java")));
    }

    @Test
    void create_nullUserId_doesNotLookupUser() {
        VacancyRequest request = VacancyRequest.builder()
                .companyId(companyId)
                .skills(null)
                .questions(null)
                .build();
        Company company = mock(Company.class);
        Vacancy vacancy = new Vacancy();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(vacancyMapper.toEntity(request)).thenReturn(vacancy);
        when(vacancyRepository.save(any())).thenReturn(vacancy);
        when(vacancyMapper.toResponse(vacancy)).thenReturn(mock(VacancyResponse.class));

        vacancyService.create(request, null);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void create_companyNotFound_throwsCompanyNotFoundException() {
        VacancyRequest request = VacancyRequest.builder().companyId(companyId).build();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.create(request, userId))
                .isInstanceOf(CompanyNotFoundException.class);
        verify(vacancyRepository, never()).save(any());
    }

    @Test
    void update_found_updatesAndReturnsResponse() {
        VacancyRequest request = VacancyRequest.builder()
                .companyId(companyId)
                .skills(Set.of("Kotlin"))
                .questions(List.of())
                .build();
        Company company = mock(Company.class);
        Vacancy vacancy = new Vacancy();
        VacancyResponse resp = mock(VacancyResponse.class);

        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(vacancyRepository.save(vacancy)).thenReturn(vacancy);
        when(vacancyMapper.toResponse(vacancy)).thenReturn(resp);

        assertThat(vacancyService.update(vacancyId, request)).isSameAs(resp);
        verify(vacancyMapper).updateEntity(request, vacancy);
        verify(skillService).updateVacancySkills(eq(vacancy), eq(Set.of("Kotlin")));
    }

    @Test
    void update_vacancyNotFound_throwsVacancyNotFoundException() {
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.update(vacancyId, VacancyRequest.builder().companyId(companyId).build()))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void update_companyNotFound_throwsCompanyNotFoundException() {
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(new Vacancy()));
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.update(vacancyId, VacancyRequest.builder().companyId(companyId).build()))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void delete_delegatesToRepository() {
        vacancyService.delete(vacancyId);

        verify(vacancyRepository).deleteById(vacancyId);
    }

    @Test
    void archive_found_setsArchivedTrue() {
        Vacancy vacancy = new Vacancy();
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));

        vacancyService.archive(vacancyId);

        assertThat(vacancy.isArchived()).isTrue();
        verify(vacancyRepository).save(vacancy);
    }

    @Test
    void archive_notFound_throwsVacancyNotFoundException() {
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.archive(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void unarchive_found_setsArchivedFalse() {
        Vacancy vacancy = new Vacancy();
        vacancy.setArchived(true);
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));

        vacancyService.unarchive(vacancyId);

        assertThat(vacancy.isArchived()).isFalse();
        verify(vacancyRepository).save(vacancy);
    }

    @Test
    void unarchive_notFound_throwsVacancyNotFoundException() {
        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.unarchive(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void findByIdForUpdate_found_returnsVacancyRequest() {
        Vacancy vacancy = new Vacancy();
        vacancy.setTitle("Java Dev");
        vacancy.setDescription("desc");
        vacancy.setCity("Moscow");
        vacancy.setVacancyLevel(VacancyLevel.JUNIOR);

        Company company = mock(Company.class);
        when(company.getId()).thenReturn(companyId);
        vacancy.setCompany(company);

        Set<String> skills = Set.of("Java");
        when(vacancyRepository.findByIdWithAll(vacancyId)).thenReturn(Optional.of(vacancy));
        when(skillService.convertSkillsToString(any())).thenReturn(skills);

        VacancyRequest result = vacancyService.findByIdForUpdate(vacancyId);

        assertThat(result.getTitle()).isEqualTo("Java Dev");
        assertThat(result.getCompanyId()).isEqualTo(companyId);
        assertThat(result.getSkills()).isSameAs(skills);
        assertThat(result.getQuestions()).isEmpty();
    }

    @Test
    void findByIdForUpdate_notFound_throwsVacancyNotFoundException() {
        when(vacancyRepository.findByIdWithAll(vacancyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vacancyService.findByIdForUpdate(vacancyId))
                .isInstanceOf(VacancyNotFoundException.class);
    }

    @Test
    void buildFilterQuery_allNullFields_returnsEmptyString() {
        VacancyFilter filter = new VacancyFilter();
        filter.setSort(null);

        assertThat(vacancyService.buildFilterQuery(filter)).isEmpty();
    }

    @Test
    void buildFilterQuery_withTitle_appendsTitleParam() {
        VacancyFilter filter = new VacancyFilter();
        filter.setTitle("Java");
        filter.setSort(null);

        assertThat(vacancyService.buildFilterQuery(filter)).contains("title=Java");
    }

    @Test
    void buildFilterQuery_withSortAndSalary_appendsParams() {
        VacancyFilter filter = new VacancyFilter();
        filter.setSort("oldest");
        filter.setSalaryFrom(100000);
        filter.setSalaryTo(200000);

        String query = vacancyService.buildFilterQuery(filter);

        assertThat(query).contains("sort=oldest");
        assertThat(query).contains("salaryFrom=100000");
        assertThat(query).contains("salaryTo=200000");
    }

    @Test
    void update_withQuestions_addsQuestionsToVacancy() {
        QuestionRequest qr = QuestionRequest.builder()
                .text("What is OOP?")
                .difficulty("EASY")
                .category("General")
                .build();
        VacancyRequest request = VacancyRequest.builder()
                .companyId(companyId)
                .skills(null)
                .questions(List.of(qr))
                .build();
        Company company = mock(Company.class);
        Vacancy vacancy = new Vacancy();

        when(vacancyRepository.findById(vacancyId)).thenReturn(Optional.of(vacancy));
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(vacancyRepository.save(vacancy)).thenReturn(vacancy);
        when(vacancyMapper.toResponse(vacancy)).thenReturn(mock(VacancyResponse.class));

        vacancyService.update(vacancyId, request);

        assertThat(vacancy.getQuestions()).hasSize(1);
        assertThat(vacancy.getQuestions().get(0).getText()).isEqualTo("What is OOP?");
    }
}
