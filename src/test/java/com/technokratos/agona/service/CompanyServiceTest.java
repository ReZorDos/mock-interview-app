package com.technokratos.agona.service;

import com.technokratos.agona.dto.CompanyDto;
import com.technokratos.agona.exception.CompanyNotFoundException;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.mapper.CompanyMapper;
import com.technokratos.agona.model.Company;
import com.technokratos.agona.model.User;
import com.technokratos.agona.repository.CompanyRepository;
import com.technokratos.agona.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyService companyService;

    private final UUID companyId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @Test
    void findAllByUserId_delegatesToRepository() {
        List<Company> expected = List.of(mock(Company.class));
        when(companyRepository.findAllByUser_Id(userId)).thenReturn(expected);

        assertThat(companyService.findAllByUserId(userId)).isSameAs(expected);
    }

    @Test
    void search_nullQuery_returnsAll() {
        List<Company> all = List.of(mock(Company.class));
        when(companyRepository.findAll()).thenReturn(all);

        assertThat(companyService.search(null)).isSameAs(all);
        verify(companyRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void search_blankQuery_returnsAll() {
        List<Company> all = List.of(mock(Company.class));
        when(companyRepository.findAll()).thenReturn(all);

        assertThat(companyService.search("   ")).isSameAs(all);
        verify(companyRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void search_nonBlankQuery_returnsFiltered() {
        List<Company> filtered = List.of(mock(Company.class));
        when(companyRepository.findByNameContainingIgnoreCase("acme")).thenReturn(filtered);

        assertThat(companyService.search("  acme  ")).isSameAs(filtered);
        verify(companyRepository, never()).findAll();
    }

    @Test
    void findById_found_returnsCompany() {
        Company company = mock(Company.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        assertThat(companyService.findById(companyId)).isSameAs(company);
    }

    @Test
    void findById_notFound_throwsCompanyNotFoundException() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.findById(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void create_userFound_savesAndReturnsDto() {
        CompanyDto request = mock(CompanyDto.class);
        CompanyDto resultDto = mock(CompanyDto.class);
        Company entity = mock(Company.class);
        Company saved = mock(Company.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
        when(companyMapper.toEntity(request)).thenReturn(entity);
        when(companyRepository.save(entity)).thenReturn(saved);
        when(companyMapper.toDto(saved)).thenReturn(resultDto);

        assertThat(companyService.create(request, userId)).isSameAs(resultDto);
    }

    @Test
    void create_userNotFound_throwsNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.create(mock(CompanyDto.class), userId))
                .isInstanceOf(NotFoundException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void findByIdForUpdate_found_returnsDto() {
        Company company = mock(Company.class);
        CompanyDto dto = mock(CompanyDto.class);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));
        when(companyMapper.toDto(company)).thenReturn(dto);

        assertThat(companyService.findByIdForUpdate(companyId)).isSameAs(dto);
    }

    @Test
    void findByIdForUpdate_notFound_throwsCompanyNotFoundException() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.findByIdForUpdate(companyId))
                .isInstanceOf(CompanyNotFoundException.class);
    }

    @Test
    void update_found_updatesFieldsAndSaves() {
        Company company = new Company();
        CompanyDto request = CompanyDto.builder()
                .name("New Name")
                .description("New Desc")
                .logoUrl("http://logo.png")
                .build();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        companyService.update(companyId, request);

        assertThat(company.getName()).isEqualTo("New Name");
        assertThat(company.getDescription()).isEqualTo("New Desc");
        assertThat(company.getLogoUrl()).isEqualTo("http://logo.png");
        verify(companyRepository).save(company);
    }

    @Test
    void update_notFound_throwsCompanyNotFoundException() {
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> companyService.update(companyId, mock(CompanyDto.class)))
                .isInstanceOf(CompanyNotFoundException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void delete_delegatesToRepository() {
        companyService.delete(companyId);

        verify(companyRepository).deleteById(companyId);
    }
}
