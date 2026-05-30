package com.technokratos.agona.repository;

import com.technokratos.agona.dto.VacancyFilter;
import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.enums.VacancyLevel;
import com.technokratos.agona.model.Skill;
import com.technokratos.agona.model.Vacancy;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class VacancySpecification {

    public static Specification<Vacancy> build(VacancyFilter filter) {
        return Specification
                .where(notArchived())
                .and(titleContains(filter.getTitle()))
                .and(cityContains(filter.getCity()))
                .and(hasSkill(filter.getSkill()))
                .and(levelEquals(filter.getLevel()))
                .and(scheduleEquals(filter.getSchedule()))
                .and(salaryAtLeast(filter.getSalaryFrom()))
                .and(salaryAtMost(filter.getSalaryTo()));
    }

    public static Specification<Vacancy> notArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("archived"));
    }

    private static Specification<Vacancy> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) return null;
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }

    private static Specification<Vacancy> cityContains(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            return cb.like(cb.lower(root.get("city")), "%" + city.toLowerCase() + "%");
        };
    }

    private static Specification<Vacancy> hasSkill(String skill) {
        return (root, query, cb) -> {
            if (skill == null || skill.isBlank()) return null;
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Vacancy> subRoot = subquery.from(Vacancy.class);
            Join<Vacancy, Skill> skillJoin = subRoot.join("skills");
            subquery.select(cb.literal(1L))
                    .where(
                            cb.equal(subRoot.get("id"), root.get("id")),
                            cb.like(cb.lower(skillJoin.get("name")), "%" + skill.toLowerCase() + "%")
                    );
            return cb.exists(subquery);
        };
    }

    private static Specification<Vacancy> levelEquals(VacancyLevel level) {
        return (root, query, cb) -> {
            if (level == null) return null;
            return cb.equal(root.get("vacancyLevel"), level);
        };
    }

    private static Specification<Vacancy> scheduleEquals(ScheduleType schedule) {
        return (root, query, cb) -> {
            if (schedule == null) return null;
            return cb.equal(root.get("schedule"), schedule);
        };
    }

    private static Specification<Vacancy> salaryAtLeast(Integer minSalary) {
        return (root, query, cb) -> {
            if (minSalary == null) return null;
            return cb.or(
                    root.get("salaryFrom").isNull(),
                    cb.greaterThanOrEqualTo(root.get("salaryFrom"), minSalary)
            );
        };
    }

    private static Specification<Vacancy> salaryAtMost(Integer maxSalary) {
        return (root, query, cb) -> {
            if (maxSalary == null) return null;
            return cb.or(
                    root.get("salaryTo").isNull(),
                    cb.lessThanOrEqualTo(root.get("salaryTo"), maxSalary)
            );
        };
    }
}
