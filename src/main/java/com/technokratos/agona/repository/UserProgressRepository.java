package com.technokratos.agona.repository;

import com.technokratos.agona.enums.FeedbackDecision;
import com.technokratos.agona.model.UserProgress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProgressRepository extends JpaRepository<UserProgress, UUID> {

    @Query("SELECT p FROM UserProgress p JOIN FETCH p.vacancy v JOIN FETCH v.questions WHERE p.id = :id")
    Optional<UserProgress> findByIdWithQuestions(@Param("id") UUID id);

    Optional<UserProgress> findByUserIdAndVacancyId(UUID userId, UUID vacancyId);

    @Query("SELECT p FROM UserProgress p JOIN FETCH p.user JOIN FETCH p.vacancy v WHERE v.userId.id = :interviewerId AND p.finishedAt IS NOT NULL")
    List<UserProgress> findAllFinishedByInterviewer(@Param("interviewerId") UUID interviewerId);

    @EntityGraph(attributePaths = {"vacancy", "recruiterReview"})
    @Query(
        value = "SELECT p FROM UserProgress p WHERE p.user.id = :userId ORDER BY p.startedAt DESC",
        countQuery = "SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId"
    )
    Page<UserProgress> findAllByUserIdOrderByStartedAtDesc(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "recruiterReview", "vacancy"})
    @Query(
        value = "SELECT p FROM UserProgress p WHERE p.vacancy.id = :vacancyId AND p.finishedAt IS NOT NULL ORDER BY p.finishedAt DESC",
        countQuery = "SELECT COUNT(p) FROM UserProgress p WHERE p.vacancy.id = :vacancyId AND p.finishedAt IS NOT NULL"
    )
    Page<UserProgress> findFinishedByVacancyId(@Param("vacancyId") UUID vacancyId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "recruiterReview", "vacancy"})
    @Query(
        value = "SELECT p FROM UserProgress p WHERE p.vacancy.id = :vacancyId AND p.finishedAt IS NOT NULL AND p.recruiterReview IS NULL ORDER BY p.finishedAt DESC",
        countQuery = "SELECT COUNT(p) FROM UserProgress p WHERE p.vacancy.id = :vacancyId AND p.finishedAt IS NOT NULL AND p.recruiterReview IS NULL"
    )
    Page<UserProgress> findFinishedByVacancyIdWithoutFeedback(@Param("vacancyId") UUID vacancyId, Pageable pageable);

    @EntityGraph(attributePaths = {"vacancy", "recruiterReview"})
    @Query(
        value = "SELECT p FROM UserProgress p WHERE p.user.id = :userId AND p.finishedAt IS NOT NULL AND p.recruiterReview IS NULL ORDER BY p.startedAt DESC",
        countQuery = "SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId AND p.finishedAt IS NOT NULL AND p.recruiterReview IS NULL"
    )
    Page<UserProgress> findFinishedWithoutFeedbackByUserId(@Param("userId") UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"vacancy", "recruiterReview"})
    @Query(
        value = "SELECT p FROM UserProgress p WHERE p.user.id = :userId AND p.recruiterReview.decision = :decision ORDER BY p.startedAt DESC",
        countQuery = "SELECT COUNT(p) FROM UserProgress p WHERE p.user.id = :userId AND p.recruiterReview.decision = :decision"
    )
    Page<UserProgress> findByUserIdAndDecision(@Param("userId") UUID userId, @Param("decision") FeedbackDecision decision, Pageable pageable);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.vacancy.company.id = :companyId AND p.finishedAt IS NOT NULL")
    long countFinishedByCompanyId(@Param("companyId") UUID companyId);

    @Query("SELECT COUNT(p) FROM UserProgress p WHERE p.vacancy.id = :vacancyId AND p.finishedAt IS NOT NULL")
    long countFinishedByVacancyId(@Param("vacancyId") UUID vacancyId);
}
