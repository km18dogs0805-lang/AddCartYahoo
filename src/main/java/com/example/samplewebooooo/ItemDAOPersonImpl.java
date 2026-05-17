package com.example.samplewebooooo;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

public class ItemDAOPersonImpl implements ItemDAO<Item> {

    @PersistenceContext
    private EntityManager entityManager;

    public ItemDAOPersonImpl() {
        super();
    }

    @Override
    public List<Item> getAll() {
        // テーブルを指定
        Query query = entityManager.createQuery("from Item");

        List<Item> list = query.getResultList();

        entityManager.close();

        return list;
    }

    // 全データを取得メソッド
    
}
