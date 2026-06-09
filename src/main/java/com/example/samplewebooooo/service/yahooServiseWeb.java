package com.example.samplewebooooo.service;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class yahooServiseWeb {

    @Value("${yahoo.api.base-url}")
    private URI baseUrl;

    /**
     * 商品のカテゴリーリスト
     */
    
    @Value("${yahoo.api.client-id}")
    private String appId;

    private final WebClient webClient;

    public yahooServiseWeb(WebClient webClient) {
        this.webClient = webClient;
    }
    
}
