package com.technokratos.agona.repository;

import com.technokratos.agona.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    List<Company> findAllByUser_Id(UUID userId);
    Page<Company> findAllByUser_Id(UUID userId, Pageable pageable);
    List<Company> findByNameContainingIgnoreCase(String name);
}
