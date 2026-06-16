package com.example.samplewebooooo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

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
     * URL例: https://shopping.yahooapis.jp/ShoppingWebService/V3/itemSearch
     *         ?appid={client-id}&query={keyword}&results=20
     *
     * @param keyword 検索キーワード
     * @return items 商品情報 (name, price, url) のリスト
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> searchItem(String keyword) {

        // application.properties の base-url と client-id を使って URL を組み立てる
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("appid", appId)
                .queryParam("query", keyword)
                
                .build()
                .toUriString();

        System.out.println("Yahoo API リクエストURL: " + url);

        // API レスポンス全体を Map として受け取る
        Map<String, Object> response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
        System.out.println("responseの中身は、、" + response.get("totalResultsAvailable"));

        // Mapに格納したデータ（レスポンスされた結果）
        List<Map<String, String>> items = new ArrayList<>();

        // 検索結果が無い場合は、空を表示
        if (response == null) return items;

        // get('hits')で、キーから値を取り出す。 Map<キーの型, 値の型>
        // responseで得られたデータを、hitList変数に格納する
        List<Map<String, Object>> hitList = (List<Map<String, Object>>) response.get("hits");
        if (hitList == null) return items;

        // JSONから得た値を、String型に変換し、キーとして設定する（※Null値でも例外を発生させない）
        /**
         * ※以下のリスト型配列が渡される
         * ※各値を、Map<String, Object>に格納できそう
         *  {
         *    index: 0,
         *    name:  "（名称不明）"
         *    price: 0,
         *    itemUrl: "#"
         *  }
         */
        for (Map<String, Object> hit : hitList) {
            
            String index = String.valueOf(hit.getOrDefault("index", "0"));
            String name  = String.valueOf(hit.getOrDefault("name",  "（名称不明）"));
            String price = String.valueOf(hit.getOrDefault("price", "0"));
            String itemUrl = String.valueOf(hit.getOrDefault("url", "#"));

            // まとめて items に追加
            items.add(Map.of("index",index,"name", name, "price", price, "url", itemUrl));
        }

        return items;
    }
}
