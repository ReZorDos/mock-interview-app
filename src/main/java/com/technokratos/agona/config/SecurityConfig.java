package com.technokratos.agona.config;

import com.technokratos.agona.contoller.handler.CustomAccessDeniedHandler;
import com.technokratos.agona.enums.Roles;
import com.technokratos.agona.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    private static final String CSP =
            "default-src 'self'; " +
            "script-src 'self' https://cdn.jsdelivr.net https://code.jquery.com; " +
            "style-src 'self' https://cdn.jsdelivr.net https://fonts.googleapis.com 'unsafe-inline'; " +
            "font-src 'self' https://cdn.jsdelivr.net https://fonts.gstatic.com; " +
            "img-src 'self' data: blob:; " +
            "media-src 'self' blob:; " +
            "connect-src 'self'; " +
            "form-action 'self'; " +
            "frame-ancestors 'none';";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/api/**")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").hasRole(Roles.INTERVIEWER.name())
                        .requestMatchers("/login", "/register/user", "/register/interviewer").permitAll()
                        .requestMatchers("/oauth/**").permitAll()
                        .requestMatchers("/vacancy/create", "/vacancy/*/edit", "/vacancy/*/delete",
                                "/vacancy/*/archive", "/vacancy/*/unarchive").hasRole(Roles.INTERVIEWER.name())
                        .requestMatchers("/company/browse", "/company/*/vacancies").authenticated()
                        .requestMatchers("/company/**").hasRole(Roles.INTERVIEWER.name())
                        .requestMatchers("/interviewer/**").hasRole(Roles.INTERVIEWER.name())
                        .requestMatchers("/screening/**").hasRole(Roles.USER.name())
                        .requestMatchers("/feedback/**").hasRole(Roles.USER.name())
                        .requestMatchers("/audio/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/login?expired=true")
                )
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(CSP))
                        .frameOptions(Customizer.withDefaults())
                        .contentTypeOptions(Customizer.withDefaults())
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .referrerPolicy(rp -> rp
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .permissionsPolicy(pp -> pp
                                .policy("camera=(), microphone=(self), geolocation=(), payment=()")))
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of(
                "http://localhost:8081",
                "http://127.0.0.1:8081"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                "X-CSRF-TOKEN",
                "X-XSRF-TOKEN",
                "X-Requested-With"
        ));
        config.setExposedHeaders(List.of(HttpHeaders.LOCATION, "X-CSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
