package com.example.samplewebooooo.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * yt-dlp コマンド文字列を組み立てるユーティリティ。
 */
public class YtDlpCommandBuilder {

    private YtDlpCommandBuilder() {}

    /**
     * yt-dlp コマンドを組み立てる。
     *
     * @param url      m3u8 URL
     * @param outPath  出力先 mp4 パス
     * @param domain   Referer に使うドメイン
     * @param proxy    プロキシ URL (null 可)
     * @param useProxy プロキシを使うかどうか
     * @return 組み立てたコマンド文字列
     */
    public static String build(String url, String outPath, String domain, String proxy, boolean useProxy) {
        StringBuilder sb = new StringBuilder("yt-dlp");
        sb.append(" --add-header 'Referer:http://").append(domain).append("'");
        sb.append(" --output '").append(outPath).append("'");
        sb.append(" --merge-output-format mp4");
        sb.append(" --no-part");
        if (useProxy && proxy != null && !proxy.isBlank()) {
            sb.append(" --proxy '").append(proxy).append("'");
        }
        sb.append(" '").append(url).append("'");
        return sb.toString();
    }
}
