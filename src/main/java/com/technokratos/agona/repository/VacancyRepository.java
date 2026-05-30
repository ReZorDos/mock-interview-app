package com.technokratos.agona.repository;

import com.technokratos.agona.model.Vacancy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacancyRepository extends JpaRepository<Vacancy, UUID>, JpaSpecificationExecutor<Vacancy> {

    @Query("""
            SELECT DISTINCT v FROM Vacancy v
            LEFT JOIN FETCH v.skills
            LEFT JOIN FETCH v.company
            WHERE v.id = :id
            """)
    Optional<Vacancy> findByIdWithAll(@Param("id") UUID id);

    @Query("""
            SELECT v FROM Vacancy v
            LEFT JOIN FETCH v.questions
            WHERE v.id = :id
            """)
    Optional<Vacancy> findByIdWithQuestions(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT v FROM Vacancy v
            LEFT JOIN FETCH v.skills
            LEFT JOIN FETCH v.company
           """)
    List<Vacancy> findAllWithAll();

    @Query("""
            SELECT DISTINCT v FROM Vacancy v
            LEFT JOIN FETCH v.skills
            LEFT JOIN FETCH v.company
            WHERE v.userId.id = :userId
           """)
    List<Vacancy> findAllByUserId(@Param("userId") UUID userId);

    @Query("""
            SELECT DISTINCT v FROM Vacancy v
            LEFT JOIN FETCH v.skills
            LEFT JOIN FETCH v.company
            WHERE v.company.id = :companyId AND v.archived = false
           """)
    List<Vacancy> findAllByCompanyId(@Param("companyId") UUID companyId);

    @Query(value = "SELECT v FROM Vacancy v WHERE v.company.id = :companyId ORDER BY v.createdAt DESC",
           countQuery = "SELECT COUNT(v) FROM Vacancy v WHERE v.company.id = :companyId")
    Page<Vacancy> findPageByCompanyId(@Param("companyId") UUID companyId, Pageable pageable);

}
