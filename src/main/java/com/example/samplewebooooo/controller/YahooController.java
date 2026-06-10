package com.example.samplewebooooo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.example.samplewebooooo.service.yahooServiseWeb;

/**
 * YahooController: Yahoo ショッピング API を担当するコントローラー
 *
 * GET  /rakuten          - 検索フォーム表示
 * POST /rakuten/search   - キーワード検索 → 結果表示
 */
@Controller
public class YahooController {

    @Value("${yahoo.api.base-url}")
    private String baseUrl;

    @Value("${yahoo.api.client-id}")
    private String applicationId;

    @Autowired
    private yahooServiseWeb yWeb;

    // -------------------------------------------------------
    // 検索フォーム表示
    // -------------------------------------------------------

    @RequestMapping(value = "/rakuten", method = RequestMethod.GET)
    public ModelAndView searchForm(ModelAndView mav) {
        mav.setViewName("rakuten");
        mav.addObject("title", "Yahoo ショッピングで商品を検索する");
        System.out.println("baseUrl: " + baseUrl);
        System.out.println("applicationId: " + applicationId);
        return mav;
    }

    // -------------------------------------------------------
    // キーワード検索
    // -------------------------------------------------------

    @RequestMapping(value = "/rakuten/search", method = RequestMethod.POST)
    public ModelAndView searchItems(@RequestParam String keyword,
                                    ModelAndView mav) {
        mav.setViewName("rakuten");

        try {
            List<Map<String, String>> items = yWeb.searchItem(keyword);
            System.out.println("Yahoo API 検索結果件数: " + items.size());

            mav.addObject("keyword", keyword);
            mav.addObject("items", items);

            if (items.isEmpty()) {
                mav.addObject("msg", "「" + keyword + "」に一致する商品が見つかりませんでした。");
            }

        } catch (Exception e) {
            System.err.println("Yahoo API 呼び出しエラー: " + e.getMessage());
            mav.addObject("errorMsg", "APIの呼び出しに失敗しました: " + e.getMessage());
        }

        return mav;
    }
}
