package com.technokratos.agona.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yandex.oauth")
public class YandexOAuthProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}
