package com.technokratos.agona.model;

import com.technokratos.agona.enums.Difficulty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "question")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Question extends AbstractEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(length = 100)
    private String category;

    @ManyToOne
    @JoinColumn(name = "vacancy_id", nullable = false)
    private Vacancy vacancy;

    @OneToMany(mappedBy = "question")
    private List<UserAnswer> answers = new ArrayList<>();

}
