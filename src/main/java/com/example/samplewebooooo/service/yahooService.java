package com.example.samplewebooooo.service;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Service
public class yahooService {

    // ✅ @Value の値はコンストラクタ実行後に注入されるため、
    //    フィールド宣言時には使わない
    @Value("${yahoo.api.base-url}")
    private String baseUrl;

    /**
     * 商品のカテゴリーリスト
     */
    
    @Value("${yahoo.api.client-id}")
    private String appId;

    private final WebClient webClient = WebClient.create();

    public static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("1",     "すべてのカテゴリから");
        CATEGORIES.put("13457", "ファッション");
        CATEGORIES.put("2498",  "食品");
        CATEGORIES.put("2500",  "ダイエット、健康");
        CATEGORIES.put("2501",  "コスメ、香水");
        CATEGORIES.put("2502",  "パソコン、周辺機器");
        CATEGORIES.put("2504",  "AV機器、カメラ");
        CATEGORIES.put("2505",  "家電");
        CATEGORIES.put("2506",  "家具、インテリア");
        CATEGORIES.put("2507",  "花、ガーデニング");
        CATEGORIES.put("2508",  "キッチン、生活雑貨、日用品");
        CATEGORIES.put("2503",  "DIY、工具、文具");
        CATEGORIES.put("2509",  "ペット用品、生き物");
        CATEGORIES.put("2510",  "楽器、趣味、学習");
        CATEGORIES.put("2511",  "ゲーム、おもちゃ");
        CATEGORIES.put("2497",  "ベビー、キッズ、マタニティ");
        CATEGORIES.put("2512",  "スポーツ");
        CATEGORIES.put("2513",  "レジャー、アウトドア");
        CATEGORIES.put("2514",  "自転車、車、バイク用品");
        CATEGORIES.put("2516",  "CD、音楽ソフト");
        CATEGORIES.put("2517",  "DVD、映像ソフト");
        CATEGORIES.put("10002", "本、雑誌、コミック");
    }

    public static final Map<String, String> SORT_ORDER = new LinkedHashMap<>();
    static {
        SORT_ORDER.put("-score", "おすすめ順");
        SORT_ORDER.put("+price", "商品価格が安い順");
        SORT_ORDER.put("-price", "商品価格が高い順");
        SORT_ORDER.put("+name",  "ストア名昇順");
        SORT_ORDER.put("-name",  "ストア名降順");
        SORT_ORDER.put("-sold",  "売れ筋順");
    }
    
    /**
     * 特殊文字をHTMLエンティティに変換する（PHP の htmlspecialchars 相当）
     * & を必ず最初に変換することで二重エスケープを防ぐ
     */
    public static String h(String str) {
        if (str == null) return "";
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;");
    }
    


}
