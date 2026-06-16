package com.example.samplewebooooo.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class webClientConfig {

    // エンドポイント
    @Value("${yahoo.api.base-url}")
    private String baseUrl;

    // APIキー
    @Value("${yahoo.api.client-id}")
    private String appId;

    /**
     * エンドポイントURLをビルド
     * @return
     */
    @Bean
    public WebClient yahooWebClient() {
        return WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

}
