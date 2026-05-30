package com.technokratos.agona.contoller;

import com.technokratos.agona.service.YandexOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/oauth/yandex")
@RequiredArgsConstructor
public class YandexOAuthController {

    private final YandexOAuthService yandexOAuthService;

    @GetMapping
    public String redirect(HttpSession session) {
        return "redirect:" + yandexOAuthService.startAuthorization(session);
    }

    @GetMapping("/callback")
    public String callback(@RequestParam String code,
                           @RequestParam String state,
                           HttpSession session,
                           HttpServletRequest request) {
        yandexOAuthService.handleCallback(code, state, session, request);
        return "redirect:/home";
    }
}
