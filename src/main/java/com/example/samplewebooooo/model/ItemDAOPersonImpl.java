package com.example.samplewebooooo.model;

import java.util.List;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
public class ItemDAOPersonImpl implements ItemDAO<Item> {

    
    @PersistenceContext  // Beanを取得し、自動でフィールドに割り当てる
    private EntityManager entityManager;

    public ItemDAOPersonImpl() {
        super();
    }

    /*
     *   全てのデータを取得する
     */
    @Override
    public List<Item> getAll() {

        // テーブルを指定
        Query query = entityManager.createQuery("from Item");
        
        // クエリの結果をリストに格納する
        List<Item> list = query.getResultList();

        // EntityManagerを閉じる
        entityManager.close();

        // リストを返す
        return list;
    }

    /*
     *   IDでデータを取得する
     */
    @Override
    public Item findById(long id) {

        // idに対応するデータを取得するクエリを実行する
        return (Item)entityManager.createQuery("from Item where id = " + id).getSingleResult();
    }

    /*
     *   名前でデータを取得する
     */
    @Override
    public List<Item> findByName(String fstr) {
        // nameに対応するデータを取得するクエリを実行する
        List<Item> list = null;

        // クエリを作成する（fstrをパラメータとして渡す）
        String qstr = "from Item where name = :fstr";

        // クエリを実行する
        Query query = entityManager.createQuery(qstr)
                                    .setParameter("fstr", Long.parseLong(fstr));
        
        // クエリの結果をリストに格納する
        list = query.getResultList();

        return list;
    }

    /*
    *   IDでデータを削除する
    */
    @Override
    public void deleteById(long id) {

         // idに対応するデータを取得し、削除する
        Item item = findById(id);

        // データが存在する場合は削除する
        if (item != null) {

            entityManager.remove(item);

        }

        // EntityManagerを閉じる
        entityManager.close();

    }

    /**
     * IDや名前でデータを検索する
     */
    @Override
    public List<Item> find(String fstr) { 

        // listの初期化
        List<Item> list = null;
        
        // クエリ文を作成し、変数に格納（fstrをパラメータとして渡す）
        String qstr = "from Item where id = :fid or name like :fname";
        System.out.println("クエリ文: " + qstr);
        Long fid = 0L;
        // idをLong型に変換し、できなければ例外をキャッチする
        try { 

            fid = Long.parseLong(fstr);
            System.out.println("fidは～～～～: " + fid);

        } catch (NumberFormatException e) { 

            // 例外が発生した場合は、fidを0にする
            e.printStackTrace();
            System.out.println("fidは～～～～: " + fid);

        }

        Query query = entityManager.createQuery(qstr)
                                    .setParameter("fid", fid)
                                    .setParameter("fname", "%" + fstr + "%");

        // クエリを実行し、結果をリストに格納する
        list = query.getResultList();

        return list;
    }

    /*
    *   データの合計を取得するクエリを実行する
    */
    @Override
    public long totalPrice() {

        // データベースから、価格と数量を掛け合わせた値の合計を取得するクエリを実行する
        return (long)entityManager.createQuery("SELECT SUM(price * volume) FROM Item").getSingleResult();
    }
    

    
}
