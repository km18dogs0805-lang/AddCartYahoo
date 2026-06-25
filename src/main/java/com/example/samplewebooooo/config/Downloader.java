package com.example.samplewebooooo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.file.*;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/**
 * ダウンローダー抽象基底クラス。
 *
 * 使用手順:
 *   1. downloadInfo()  → 元データ取得 → download_info.json に保存
 *   2. downloadM3u8()  → yt-dlp で m3u8 → mp4 直接生成
 */
public abstract class Downloader {

    protected static final Logger logger = LoggerFactory.getLogger(Downloader.class);

    protected final String   path;
    protected final String   proxy;
    protected final Duration timeout;
    protected String         domain = "";

    private final HttpClient httpClient;

    private static final Map<String, String> DEFAULT_HEADERS = Map.of(
        "User-Agent",      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Accept",          "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Accept-Language", "en-US,en;q=0.5"
    );

    /**
     * @param path       保存先ルートパス (例: /vol2/user/missav)
     * @param proxy      プロキシ URL (不要な場合は null)
     * @param timeoutSec タイムアウト秒数
     */
    public Downloader(String path, String proxy, int timeoutSec) {
        this.path    = path;
        this.proxy   = proxy;
        this.timeout = Duration.ofSeconds(timeoutSec);

        HttpClient.Builder builder = HttpClient.newBuilder()
            .connectTimeout(this.timeout)
            .followRedirects(HttpClient.Redirect.NORMAL);

        if (proxy != null && !proxy.isBlank()) {
            URI proxyUri = URI.create(proxy);
            builder.proxy(ProxySelector.of(
                new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort())
            ));
        }

        this.httpClient = builder.build();
    }

    // ----------------------------------------------------------
    // 抽象メソッド (サブクラスで実装)
    // ----------------------------------------------------------

    public abstract String getDownloaderName();

    /** avid を元に URL を組み立てて HTML を取得する */
    public abstract Optional<String> getHTML(String avid);

    /** HTML を解析して AVDownloadInfo を返す */
    public abstract Optional<AVDownloadInfo> parseHTML(String html, String avid);

    // ----------------------------------------------------------
    // ドメイン設定
    // ----------------------------------------------------------

    public boolean setDomain(String domain) {
        if (domain != null && !domain.isBlank()) {
            this.domain = domain;
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------
    // downloadInfo: 元データ取得 → JSON 保存
    // ----------------------------------------------------------

    public Optional<AVDownloadInfo> downloadInfo(String avid) {
        avid = avid.toUpperCase();
        Path avDir = Path.of(path, avid);

        try {
            Files.createDirectories(avDir);
        } catch (IOException e) {
            logger.error("ディレクトリ作成失敗: {}", e.getMessage());
            return Optional.empty();
        }

        Optional<String> htmlOpt = getHTML(avid);
        if (htmlOpt.isEmpty()) {
            logger.error("HTML 取得失敗");
            return Optional.empty();
        }

        String html = htmlOpt.get();
        try {
            Files.writeString(avDir.resolve(avid + ".html"), html);
        } catch (IOException e) {
            logger.warn("HTML 保存失敗: {}", e.getMessage());
        }

        Optional<AVDownloadInfo> infoOpt = parseHTML(html, avid);
        if (infoOpt.isEmpty()) {
            logger.error("元データ解析失敗");
            return Optional.empty();
        }

        AVDownloadInfo info = infoOpt.get();
        info.avid = info.avid.toUpperCase();
        info.toJson(avDir.resolve("download_info.json").toString());
        logger.info("download_info.json に保存しました");

        return Optional.of(info);
    }

    // ----------------------------------------------------------
    // downloadM3u8: yt-dlp で m3u8 → mp4 直接生成
    // ----------------------------------------------------------

    /**
     * yt-dlp を使って m3u8 を mp4 へダウンロード。
     * 中間 .ts ファイルは生成されない。
     *
     * @param url  m3u8 URL
     * @param avid 番号
     * @return 成功なら true
     */
    public boolean downloadM3u8(String url, String avid) {
        Path   avDir   = Path.of(path, avid);
        String outPath = avDir.resolve(avid + ".mp4").toString();

        try {
            Files.createDirectories(avDir);
        } catch (IOException e) {
            logger.error("ディレクトリ作成失敗: {}", e.getMessage());
            return false;
        }

        boolean useProxy = (proxy != null && !proxy.isBlank());

        // 1回目
        String command = YtDlpCommandBuilder.build(url, outPath, domain, proxy, useProxy);
        logger.debug("実行コマンド: {}", command);
        if (CommandRunner.run(command)) {
            return true;
        }

        // リトライ: プロキシ設定を反転
        logger.info("リトライ: プロキシ設定を反転して再試行");
        String retryCommand = YtDlpCommandBuilder.build(url, outPath, domain, proxy, !useProxy);
        logger.debug("リトライコマンド: {}", retryCommand);
        return CommandRunner.run(retryCommand);
    }

    // ----------------------------------------------------------
    // fetchHTML: HTTP GET で HTML 文字列を取得
    // ----------------------------------------------------------

    protected Optional<String> fetchHTML(String url, String referer) {
        logger.debug("fetch url: {}", url);
        try {
            HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .GET();

            DEFAULT_HEADERS.forEach(reqBuilder::header);
            if (referer != null && !referer.isBlank()) {
                reqBuilder.header("Referer", referer);
            }

            HttpResponse<String> response = httpClient.send(
                reqBuilder.build(),
                HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() >= 400) {
                logger.error("HTTP エラー: {}", response.statusCode());
                return Optional.empty();
            }
            return Optional.of(response.body());

        } catch (IOException | InterruptedException e) {
            logger.error("リクエスト失敗: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    /** Referer なしのオーバーロード */
    protected Optional<String> fetchHTML(String url) {
        return fetchHTML(url, "");
    }
}
