package com.technokratos.agona.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Company extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Vacancy> vacancyList = new ArrayList<>();

}
