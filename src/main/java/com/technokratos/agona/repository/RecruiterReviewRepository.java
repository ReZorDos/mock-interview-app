package com.technokratos.agona.repository;

import com.technokratos.agona.model.RecruiterReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecruiterReviewRepository extends JpaRepository<RecruiterReview, UUID> {

    Optional<RecruiterReview> findByProgressId(UUID progressId);

    @Query("SELECT r FROM RecruiterReview r JOIN FETCH r.progress p JOIN FETCH p.vacancy WHERE p.user.id = :userId ORDER BY r.createdAt DESC")
    List<RecruiterReview> findAllByProgressUserId(@Param("userId") UUID userId);
}
