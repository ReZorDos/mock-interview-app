package com.technokratos.agona.repository;

import com.technokratos.agona.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID> {
    Optional<Skill> findByName(String name);
}
