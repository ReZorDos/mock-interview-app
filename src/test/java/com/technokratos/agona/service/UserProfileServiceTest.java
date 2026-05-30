package com.technokratos.agona.service;

import com.technokratos.agona.dto.UserProfileForm;
import com.technokratos.agona.dto.UserProfileView;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.mapper.UserProfileMapper;
import com.technokratos.agona.model.User;
import com.technokratos.agona.model.UserProfile;
import com.technokratos.agona.repository.UserProfileRepository;
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
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileMapper userProfileMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getFormByUserId_profileExists_returnsForm() {
        UserProfile profile = UserProfile.builder().age(25).build();
        UserProfileForm form = UserProfileForm.builder().age(25).build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userProfileMapper.toForm(profile)).thenReturn(form);

        UserProfileForm result = userProfileService.getFormByUserId(userId);

        assertThat(result.getAge()).isEqualTo(25);
    }

    @Test
    void getFormByUserId_profileAbsent_returnsEmptyForm() {
        UserProfile emptyProfile = UserProfile.builder().build();
        UserProfileForm form = UserProfileForm.builder().build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userProfileMapper.toForm(any(UserProfile.class))).thenReturn(form);

        UserProfileForm result = userProfileService.getFormByUserId(userId);

        assertThat(result).isNotNull();
    }

    @Test
    void getViewByUserId_profileExists_mapsFields() {
        UserProfile profile = UserProfile.builder()
                .age(30)
                .education("МГУ")
                .experience("2 года")
                .aboutMe("Разработчик")
                .skills(List.of("Java", "Spring"))
                .build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileView view = userProfileService.getViewByUserId(userId);

        assertThat(view.age()).isEqualTo(30);
        assertThat(view.education()).isEqualTo("МГУ");
        assertThat(view.skills()).containsExactly("Java", "Spring");
    }

    @Test
    void getViewByUserId_profileAbsent_returnsEmptyView() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        UserProfileView view = userProfileService.getViewByUserId(userId);

        assertThat(view).isNotNull();
        assertThat(view.skills()).isEmpty();
    }

    @Test
    void getViewByUserId_nullSkills_returnsEmptySkillsList() {
        UserProfile profile = UserProfile.builder().skills(null).build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileView view = userProfileService.getViewByUserId(userId);

        assertThat(view.skills()).isEmpty();
    }

    @Test
    void getViewByUserId_blankSkills_returnsEmptySkillsList() {
        UserProfile profile = UserProfile.builder().skills(List.of()).build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        UserProfileView view = userProfileService.getViewByUserId(userId);

        assertThat(view.skills()).isEmpty();
    }

    @Test
    void save_profileExists_updatesAndSaves() {
        UserProfile existing = UserProfile.builder().build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        UserProfileForm form = UserProfileForm.builder()
                .age(22).education("СПбГУ").experience("1 год").aboutMe("Студент").skills("Python, Django")
                .build();

        userProfileService.save(userId, form);

        verify(userProfileRepository).save(existing);
        assertThat(existing.getAge()).isEqualTo(22);
        assertThat(existing.getSkills()).containsExactly("Python", "Django");
    }

    @Test
    void save_profileAbsent_userExists_createsNewProfile() {
        User user = mock(User.class);
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        UserProfileForm form = UserProfileForm.builder().age(20).skills("Java").build();

        userProfileService.save(userId, form);

        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    void save_profileAbsent_userNotFound_throwsNotFoundException() {
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.save(userId, UserProfileForm.builder().build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void save_blankSkills_normalizesToNull() {
        UserProfile existing = UserProfile.builder().build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        UserProfileForm form = UserProfileForm.builder().skills("   ").build();

        userProfileService.save(userId, form);

        assertThat(existing.getSkills()).isNull();
    }

    @Test
    void save_nullSkills_normalizesToNull() {
        UserProfile existing = UserProfile.builder().build();
        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        UserProfileForm form = UserProfileForm.builder().skills(null).build();

        userProfileService.save(userId, form);

        assertThat(existing.getSkills()).isNull();
    }
}
