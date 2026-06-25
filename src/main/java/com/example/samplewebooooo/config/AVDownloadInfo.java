package com.example.samplewebooooo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

/**
 * ダウンロード元データクラス (Python の dataclass 相当)
 * avid のみ必須。title・m3u8 はデバッグ用。
 */
public class AVDownloadInfo {

    private static final Logger logger = LoggerFactory.getLogger(AVDownloadInfo.class);

    public String m3u8  = "";
    public String title = "";
    public String avid  = "";

    @Override
    public String toString() {
        return "=== 元データ詳細 ===\n"
             + "番号: "     + (avid.isEmpty()  ? "不明" : avid)  + "\n"
             + "タイトル: " + (title.isEmpty() ? "不明" : title) + "\n"
             + "M3U8: "    + (m3u8.isEmpty()  ? "なし" : m3u8);
    }

    /**
     * JSON ファイルへシリアライズする。
     *
     * @param filePath 出力先パス
     * @return 成功なら true
     */
    public boolean toJson(String filePath) {
        try {
            Path path = Path.of(filePath);
            Files.createDirectories(path.getParent());
            new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(path.toFile(), this);
            return true;
        } catch (IOException e) {
            logger.error("JSON シリアライズ失敗: {}", e.getMessage());
            return false;
        }
    }
}
