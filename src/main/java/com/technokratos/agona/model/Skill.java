package com.technokratos.agona.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skill")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill extends AbstractEntity {

    @Column(nullable = false, length = 60)
    private String name;

    @ManyToMany(mappedBy = "skills")
    private Set<Vacancy> vacancies = new HashSet<>();

}
