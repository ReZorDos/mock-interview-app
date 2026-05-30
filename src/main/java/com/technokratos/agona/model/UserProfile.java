package com.technokratos.agona.model;

import com.technokratos.agona.converter.SkillsConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "age")
    private Integer age;

    @Column(name = "education", length = 500)
    private String education;

    @Column(name = "experience", columnDefinition = "TEXT")
    private String experience;

    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;

    @Convert(converter = SkillsConverter.class)
    @Column(name = "skills", columnDefinition = "TEXT")
    private List<String> skills;

}
