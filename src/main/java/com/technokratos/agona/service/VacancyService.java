package com.technokratos.agona.service;

import com.technokratos.agona.dto.QuestionRequest;
import com.technokratos.agona.dto.VacancyFilter;
import com.technokratos.agona.dto.VacancyRequest;
import com.technokratos.agona.dto.VacancyResponse;
import com.technokratos.agona.enums.Difficulty;
import com.technokratos.agona.exception.CompanyNotFoundException;
import com.technokratos.agona.exception.VacancyNotFoundException;
import com.technokratos.agona.mapper.VacancyMapper;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.Question;
import com.technokratos.agona.model.Vacancy;
import com.technokratos.agona.repository.CompanyRepository;
import com.technokratos.agona.repository.UserRepository;
import com.technokratos.agona.repository.VacancyRepository;
import com.technokratos.agona.repository.VacancySpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VacancyService {

    private static final int PAGE_SIZE = 10;

    private final VacancyRepository vacancyRepository;
    private final VacancyMapper vacancyMapper;
    private final CompanyRepository companyRepository;
    private final SkillService skillService;
    private final UserRepository userRepository;

    public List<VacancyResponse> findAll() {
        return vacancyMapper.toResponse(vacancyRepository.findAllWithAll());
    }

    public Page<VacancyResponse> findAllFiltered(VacancyFilter filter, int page) {
        Sort sort = buildSort(filter.getSort());
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, sort);
        Specification<Vacancy> spec = VacancySpecification.build(filter);
        return vacancyRepository.findAll(spec, pageable).map(vacancyMapper::toResponse);
    }

    public Page<VacancyResponse> findAllByUserId(UUID userId, int page) {
        PageRequest pageable = PageRequest.of(Math.max(page - 1, 0), PAGE_SIZE, Sort.by("createdAt").descending());
        Specification<Vacancy> spec = (root, query, cb) ->
                cb.equal(root.get("userId").get("id"), userId);
        return vacancyRepository.findAll(spec, pageable).map(vacancyMapper::toResponse);
    }

    public String buildFilterQuery(VacancyFilter filter) {
        StringBuilder sb = new StringBuilder();
        appendIfNotBlank(sb, "title", filter.getTitle());
        appendIfNotBlank(sb, "city", filter.getCity());
        appendIfNotBlank(sb, "skill", filter.getSkill());
        if (filter.getLevel() != null)      sb.append("&level=").append(filter.getLevel());
        if (filter.getSchedule() != null)   sb.append("&schedule=").append(filter.getSchedule());
        if (filter.getSalaryFrom() != null) sb.append("&salaryFrom=").append(filter.getSalaryFrom());
        if (filter.getSalaryTo() != null)   sb.append("&salaryTo=").append(filter.getSalaryTo());
        if (filter.getSort() != null)       sb.append("&sort=").append(filter.getSort());
        return sb.toString();
    }

    private Sort buildSort(String sortType) {
        if (sortType == null) return Sort.by("createdAt").descending();
        return switch (sortType) {
            case "oldest"      -> Sort.by("createdAt").ascending();
            case "salary_asc"  -> Sort.by(Sort.Order.asc("salaryFrom").nullsLast())
                                      .and(Sort.by("createdAt").descending());
            case "salary_desc" -> Sort.by(Sort.Order.desc("salaryFrom").nullsLast())
                                      .and(Sort.by("createdAt").descending());
            default            -> Sort.by("createdAt").descending();
        };
    }

    private void appendIfNotBlank(StringBuilder sb, String key, String value) {
        if (value != null && !value.isBlank()) {
            sb.append("&").append(key).append("=")
              .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
    }

    public List<VacancyResponse> findAllByUserId(UUID userId) {
        return vacancyMapper.toResponse(vacancyRepository.findAllByUserId(userId));
    }

    public List<VacancyResponse> findAllByCompanyId(UUID companyId) {
        return vacancyMapper.toResponse(vacancyRepository.findAllByCompanyId(companyId));
    }

    @Transactional
    public VacancyResponse findById(UUID id) {
        Vacancy vacancy = vacancyRepository.findByIdWithAll(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        vacancyRepository.findByIdWithQuestions(id);
        return vacancyMapper.toResponse(vacancy);
    }

    @Transactional
    public VacancyResponse create(VacancyRequest request, UUID userId) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(request.getCompanyId()));
        Vacancy vacancy = vacancyMapper.toEntity(request);
        vacancy.setCompany(company);
        if (userId != null) {
            userRepository.findById(userId).ifPresent(vacancy::setUserId);
        }

        Vacancy savedVacancy = vacancyRepository.save(vacancy);
        skillService.updateVacancySkills(savedVacancy, request.getSkills());
        addQuestionsToVacancy(savedVacancy, request.getQuestions());

        return vacancyMapper.toResponse(vacancyRepository.save(savedVacancy));
    }

    @Transactional
    public VacancyResponse update(UUID id, VacancyRequest request) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CompanyNotFoundException(request.getCompanyId()));
        vacancyMapper.updateEntity(request, vacancy);
        vacancy.setCompany(company);

        vacancy.getQuestions().clear();
        addQuestionsToVacancy(vacancy, request.getQuestions());

        skillService.updateVacancySkills(vacancy, request.getSkills());

        return vacancyMapper.toResponse(vacancyRepository.save(vacancy));
    }

    public void delete(UUID id) {
        vacancyRepository.deleteById(id);
    }

    @Transactional
    public void archive(UUID id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        vacancy.setArchived(true);
        vacancyRepository.save(vacancy);
    }

    @Transactional
    public void unarchive(UUID id) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        vacancy.setArchived(false);
        vacancyRepository.save(vacancy);
    }

    @Transactional
    public VacancyRequest findByIdForUpdate(UUID id) {
        Vacancy vacancy = vacancyRepository.findByIdWithAll(id)
                .orElseThrow(() -> new VacancyNotFoundException(id));
        Set<String> skills = skillService.convertSkillsToString(vacancy.getSkills());
        List<QuestionRequest> questions = vacancy.getQuestions().stream()
                .map(q -> QuestionRequest.builder()
                        .text(q.getText())
                        .difficulty(q.getDifficulty().name())
                        .category(q.getCategory())
                        .build())
                .collect(Collectors.toList());
        return VacancyRequest.builder()
                .id(vacancy.getId())
                .title(vacancy.getTitle())
                .description(vacancy.getDescription())
                .city(vacancy.getCity())
                .salaryFrom(vacancy.getSalaryFrom())
                .salaryTo(vacancy.getSalaryTo())
                .schedule(vacancy.getSchedule())
                .vacancyLevel(vacancy.getVacancyLevel())
                .companyId(vacancy.getCompany().getId())
                .skills(skills)
                .questions(questions)
                .build();
    }

    private void addQuestionsToVacancy(Vacancy vacancy, List<QuestionRequest> questionRequests) {
        if (questionRequests == null || questionRequests.isEmpty()) return;
        for (QuestionRequest qr : questionRequests) {
            if (qr.getText() == null || qr.getText().isBlank()) continue;
            Question question = Question.builder()
                    .text(qr.getText())
                    .difficulty(qr.getDifficulty() != null && !qr.getDifficulty().isBlank()
                            ? Difficulty.valueOf(qr.getDifficulty())
                            : Difficulty.EASY)
                    .category(qr.getCategory())
                    .vacancy(vacancy)
                    .build();
            vacancy.getQuestions().add(question);
        }
    }
}