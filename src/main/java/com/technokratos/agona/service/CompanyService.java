package com.technokratos.agona.service;

import com.technokratos.agona.dto.CompanyDto;
import com.technokratos.agona.exception.CompanyNotFoundException;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.mapper.CompanyMapper;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.User;
import com.technokratos.agona.repository.CompanyRepository;
import com.technokratos.agona.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyMapper companyMapper;

    public List<Company> findAllByUserId(UUID userId) {
        return companyRepository.findAllByUser_Id(userId);
    }

    public List<Company> search(String q) {
        if (q == null || q.isBlank()) return companyRepository.findAll();
        return companyRepository.findByNameContainingIgnoreCase(q.trim());
    }

    public CompanyDto findById(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
        return companyMapper.toDto(company);
    }

    public CompanyDto create(CompanyDto request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Company company = companyMapper.toEntity(request);
        company.setUser(user);
        return companyMapper.toDto(companyRepository.save(company));
    }

    public CompanyDto findByIdForUpdate(UUID id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
        return companyMapper.toDto(company);
    }

    @Transactional
    public void update(UUID id, CompanyDto request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
        company.setName(request.getName());
        company.setDescription(request.getDescription());
        company.setLogoUrl(request.getLogoUrl());
        companyRepository.save(company);
    }

    public void delete(UUID id) {
        companyRepository.deleteById(id);
    }
}
