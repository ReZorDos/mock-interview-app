package com.technokratos.agona.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "current_question_index")
    private Integer currentQuestionIndex = 0;

    @Builder.Default
    @OneToMany(mappedBy = "progress", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAnswer> answers = new ArrayList<>();

    @OneToOne(mappedBy = "progress", cascade = CascadeType.ALL, orphanRemoval = true)
    private RecruiterReview recruiterReview;

}
