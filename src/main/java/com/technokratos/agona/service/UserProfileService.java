package com.technokratos.agona.service;

import com.technokratos.agona.dto.UserProfileForm;
import com.technokratos.agona.dto.UserProfileView;
import com.technokratos.agona.exception.NotFoundException;
import com.technokratos.agona.exception.UserProfileNotFoundException;
import com.technokratos.agona.mapper.UserProfileMapper;
import com.technokratos.agona.model.User;
import com.technokratos.agona.model.UserProfile;
import com.technokratos.agona.repository.UserProfileRepository;
import com.technokratos.agona.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileMapper userProfileMapper;

    public UserProfileForm getFormByUserId(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().build());
        return userProfileMapper.toForm(profile);
    }

    public UserProfileView getViewByUserId(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElse(UserProfile.builder().build());
        return UserProfileView.builder()
                .age(profile.getAge())
                .education(profile.getEducation())
                .experience(profile.getExperience())
                .aboutMe(profile.getAboutMe())
                .skills(profile.getSkills() != null ? profile.getSkills() : List.of())
                .build();
    }

    @Transactional
    public void save(UUID userId, UserProfileForm form) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
                    return UserProfile.builder().user(user).build();
                });

        profile.setAge(form.getAge());
        profile.setEducation(form.getEducation());
        profile.setExperience(form.getExperience());
        profile.setAboutMe(form.getAboutMe());
        profile.setSkills(parseSkillsInput(form.getSkills()));
        userProfileRepository.save(profile);
    }

    private List<String> parseSkillsInput(String raw) {
        if (raw == null || raw.isBlank()) return null;
        List<String> result = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        return result.isEmpty() ? null : result;
    }
}
