package com.example.samplewebooooo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class yahooServiseWeb {

    @Value("${yahoo.api.base-url}")
    private String baseUrl;

    @Value("${yahoo.api.client-id}")
    private String appId;

    private final WebClient webClient;

    public yahooServiseWeb(@Qualifier("yahooWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Yahoo ショッピング API で商品を検索し、結果リストを返す
     *
     * @param keyword 検索キーワード
     * @return 商品情報 (name, price, url) のリスト
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> searchItem(String keyword) {

        // API レスポンス全体を Map として受け取る
        Map<String, Object> response = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("shopping.yahooapis.jp")
                        .path("/ShoppingWebService/V3/itemSearch")
                        .queryParam("appid", appId)
                        .queryParam("query", keyword)
                        .queryParam("results", 20)  // 取得件数
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, String>> items = new ArrayList<>();

        if (response == null) return items;

        // hits 配列を取得
        Map<String, Object> hits = (Map<String, Object>) response.get("hits");
        if (hits == null) return items;

        List<Map<String, Object>> hitList = (List<Map<String, Object>>) hits.get("hit");
        if (hitList == null) return items;

        for (Map<String, Object> hit : hitList) {
            String name  = String.valueOf(hit.getOrDefault("name", "（名称不明）"));
            String price = String.valueOf(hit.getOrDefault("price", "0"));
            String url   = String.valueOf(hit.getOrDefault("url",  "#"));

            items.add(Map.of("name", name, "price", price, "url", url));
        }

        return items;
    }
}
