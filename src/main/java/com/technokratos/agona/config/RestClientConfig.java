package com.technokratos.agona.config;

import com.technokratos.agona.config.properties.AssemblyAiProperties;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.ssl.SSLContexts;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import javax.net.ssl.SSLContext;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final AssemblyAiProperties properties;

    @Bean
    public RestClient assemblyAiRestClient() {
        SSLContext sslContext = SSLContexts.createSystemDefault();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                                .setSSLSocketFactory(
                                        SSLConnectionSocketFactoryBuilder.create()
                                                .setSslContext(sslContext)
                                                .build()
                                )
                                .build()
                )
                .build();

        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Authorization", properties.getApiKey())
                .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
    }

    @Bean
    public RestClient yandexRestClient() {
        return RestClient.create();
    }
}
