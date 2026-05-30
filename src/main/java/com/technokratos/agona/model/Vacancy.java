package com.technokratos.agona.model;

import com.technokratos.agona.enums.ScheduleType;
import com.technokratos.agona.enums.VacancyLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "vacancy")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Vacancy extends AbstractEntity {

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String city;

    @Column(name = "salary_from")
    private Integer salaryFrom;

    @Column(name = "salary_to")
    private Integer salaryTo;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ScheduleType schedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VacancyLevel vacancyLevel;

    @OneToMany(mappedBy = "vacancy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions = new ArrayList<>();

    @BatchSize(size = 20)
    @ManyToMany
    @JoinTable(name = "vacancy_skill",
                joinColumns = @JoinColumn(name = "vacancy_id"),
                inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @OneToMany(mappedBy = "vacancy")
    private List<UserProgress> progresses = new ArrayList<>();

    @Column(nullable = false)
    private boolean archived = false;

}
