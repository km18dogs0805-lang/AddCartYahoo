package com.example.samplewebooooo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.example.samplewebooooo.model.Item;
import com.example.samplewebooooo.model.ItemDAOPersonImpl;
import com.example.samplewebooooo.repositories.ItemRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

/**
 * ItemController: 在庫アイテムの CRUD 操作を担当するコントローラー
 *
 * GET  /             - 一覧表示 (main.html)
 * POST /result       - 登録・更新
 * POST /main         - DAO 経由で一覧取得
 * GET  /findresult   - 検索フォーム表示
 * POST /findresult   - キーワード検索
 * POST /delete_result/{id} - 削除
 * POST /sum_result   - 合計金額表示
 */
@Controller
public class ItemController {

    @Autowired
    private ItemRepository repository;

    @Autowired
    private ItemDAOPersonImpl dao;

    // -------------------------------------------------------
    // 一覧表示
    // -------------------------------------------------------

    @RequestMapping("/")
    public ModelAndView mainDisplay(ModelAndView mav) {
        mav.setViewName("main");
        mav.addObject("data", repository.findAll());
        return mav;
    }

    // -------------------------------------------------------
    // 登録・更新
    // -------------------------------------------------------

    @PostMapping("/result")
    @Transactional
    public ModelAndView resultDisplay(@ModelAttribute("formModel") Item item,
                                      ModelAndView mav) {
        repository.saveAndFlush(item);

        mav.setViewName("result");
        mav.addObject("title", "更新しました");
        mav.addObject("data", repository.findAll());
        return mav;
    }

    @PostMapping("/main")
    public ModelAndView postMethodName(@ModelAttribute("formModel") Item item,
                                       ModelAndView mav) {
        List<Item> list = dao.getAll();
        System.out.println("最後の値は・・・" + list);

        mav.setViewName("main");
        mav.addObject("data", list);
        return mav;
    }

    // -------------------------------------------------------
    // 検索
    // -------------------------------------------------------

    @GetMapping("/findresult")
    public ModelAndView findResult(ModelAndView mav,
                                   HttpServletRequest request,
                                   @ModelAttribute("formModel") @Validated Item item,
                                   BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            System.out.println("入力に誤りがあります");
            return new ModelAndView("main");
        }

        List<Item> list = repository.findAll();

        if (list == null || list.isEmpty()) {
            ModelAndView res = new ModelAndView("main");
            res.addObject("msg", "データが見つかりませんでした");
            System.out.println("データが見つかりませんでした");
            return res;
        }

        ModelAndView res = new ModelAndView("findresult");
        res.addObject("msg", "検索するデータを選択してください");
        res.addObject("items", list);
        System.out.println("削除確認" + res);
        return res;
    }

    @RequestMapping(value = "/findresult", method = RequestMethod.POST)
    public ModelAndView searchItems(HttpServletRequest request,
                                    ModelAndView mav) {
        mav.setViewName("findresult");

        String param = request.getParameter("find_str");

        if (param == null || param.isEmpty()) {
            mav.addObject("msg", "検索する値を入力してください");
            return new ModelAndView("redirect:/findresult");
        }

        mav.addObject("title", "在庫リストの検索結果");
        mav.addObject("msg", "[" + param + "]の検索結果は・・・");
        mav.addObject("items", dao.find(param));
        return mav;
    }

    // -------------------------------------------------------
    // 削除
    // -------------------------------------------------------

    @PostMapping("/delete_result/{id}")
    public ModelAndView deleteItem(@PathVariable long id, ModelAndView mav) {
        mav.setViewName("delete_result");
        mav.addObject("title", "削除しますか？");

        if (repository.existsById(id)) {
            repository.deleteById(id);
            mav.addObject("message", "削除しました");
            mav.addObject("data", repository.findAll());
        } else {
            mav.addObject("message", "データが見つかりませんでした");
        }

        return mav;
    }

    // -------------------------------------------------------
    // 合計金額
    // -------------------------------------------------------

    @PostMapping("/sum_result")
    public ModelAndView sumResult(ModelAndView mav) {
        mav.setViewName("sum_result");
        mav.addObject("title", "合計金額");

        long totalPrice = repository.count() > 0 ? dao.totalPrice() : 0;
        mav.addObject("totalPrice", totalPrice);
        return mav;
    }
}
