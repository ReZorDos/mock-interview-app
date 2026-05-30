package com.technokratos.agona.service;

import com.technokratos.agona.config.properties.YandexOAuthProperties;
import com.technokratos.agona.dto.YandexTokenResponse;
import com.technokratos.agona.dto.YandexUserInfo;
import com.technokratos.agona.enums.Roles;
import com.technokratos.agona.exception.InvalidOAuthStateException;
import com.technokratos.agona.model.User;
import com.technokratos.agona.repository.UserRepository;
import com.technokratos.agona.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexOAuthService {

    private static final String AUTH_URL      = "https://oauth.yandex.ru/authorize";
    private static final String TOKEN_URL     = "https://oauth.yandex.ru/token";
    private static final String USER_INFO_URL = "https://login.yandex.ru/info";
    private static final String STATE_KEY     = "yandex_oauth_state";

    private final YandexOAuthProperties props;
    private final RestClient yandexRestClient;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String startAuthorization(HttpSession session) {
        String state = generateState();
        session.setAttribute(STATE_KEY, state);
        return buildAuthorizationUrl(state);
    }

    public void handleCallback(String code, String state, HttpSession session, HttpServletRequest request) {
        validateState(state, session);
        String accessToken = exchangeCodeForToken(code);
        YandexUserInfo userInfo = getUserInfo(accessToken);
        User user = findOrCreateUser(userInfo);
        authenticate(user, request);
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildAuthorizationUrl(String state) {
        return new StringBuilder(AUTH_URL)
                .append("?response_type=code")
                .append("&client_id=").append(props.getClientId())
                .append("&redirect_uri=").append(URLEncoder.encode(props.getRedirectUri(), StandardCharsets.UTF_8))
                .append("&state=").append(state)
                .toString();
    }

    private void validateState(String state, HttpSession session) {
        String saved = (String) session.getAttribute(STATE_KEY);
        session.removeAttribute(STATE_KEY);
        if (saved == null || !saved.equals(state)) {
            log.warn("Невалидный OAuth state: ожидался={}, получен={}", saved, state);
            throw new InvalidOAuthStateException();
        }
    }

    private String exchangeCodeForToken(String code) {
        String body = new StringBuilder()
                .append("grant_type=authorization_code")
                .append("&code=").append(code)
                .append("&client_id=").append(props.getClientId())
                .append("&client_secret=").append(props.getClientSecret())
                .toString();

        return yandexRestClient.post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(YandexTokenResponse.class)
                .accessToken();
    }

    private YandexUserInfo getUserInfo(String accessToken) {
        return yandexRestClient.get()
                .uri(USER_INFO_URL)
                .header(HttpHeaders.AUTHORIZATION, "OAuth " + accessToken)
                .retrieve()
                .body(YandexUserInfo.class);
    }

    private User findOrCreateUser(YandexUserInfo info) {
        return userRepository.findByUsername(info.login())
                .orElseGet(() -> {
                    log.info("Создание нового пользователя через Yandex OAuth: login={}", info.login());
                    User user = new User();
                    user.setUsername(info.login());
                    user.setName(info.realName());
                    user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    user.setRole(Roles.USER);
                    return userRepository.save(user);
                });
    }

    private void authenticate(User user, HttpServletRequest request) {
        UserDetailsImpl details = UserDetailsImpl.build(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession(true)
                .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
