package com.technokratos.agona.repository;

import com.technokratos.agona.model.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {

    @Query("SELECT a FROM UserAnswer a JOIN FETCH a.question LEFT JOIN FETCH a.audioFile WHERE a.progress.id = :progressId")
    List<UserAnswer> findAllByProgressIdWithDetails(@Param("progressId") UUID progressId);
}
