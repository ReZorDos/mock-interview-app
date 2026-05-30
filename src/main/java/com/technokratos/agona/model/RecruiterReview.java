package com.technokratos.agona.model;

import com.technokratos.agona.enums.FeedbackDecision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "recruiter_review")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecruiterReview extends AbstractEntity {

    @OneToOne
    @JoinColumn(name = "progress_id", nullable = false, unique = true)
    private UserProgress progress;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", length = 20)
    private FeedbackDecision decision;

}
