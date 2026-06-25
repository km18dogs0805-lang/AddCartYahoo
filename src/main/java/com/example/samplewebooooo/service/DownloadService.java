package com.example.samplewebooooo.service;

import com.example.samplewebooooo.config.AVDownloadInfo;
import com.example.samplewebooooo.config.Downloader;
import com.example.samplewebooooo.model.DownloadStatus;
import com.example.samplewebooooo.model.DownloadTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ダウンロード処理のビジネスロジック。
 * タスク管理はオンメモリ (ConcurrentHashMap) で行う。
 */
@Service
public class DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadService.class);

    /** avid → タスク状態 */
    private final Map<String, DownloadTask> tasks = new ConcurrentHashMap<>();

    /** 非同期ダウンロード用スレッドプール */
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final Downloader downloader;

    public DownloadService(Downloader downloader) {
        this.downloader = downloader;
    }

    // ----------------------------------------------------------
    // POST /download/{avid}
    // ----------------------------------------------------------

    /**
     * ダウンロードを非同期で開始する。
     * すでに DOWNLOADING / PENDING 中の場合は何もしない。
     *
     * @return 新規登録した DownloadTask
     */
    public DownloadTask startDownload(String avid) {
        avid = avid.toUpperCase();
        String finalAvid = avid;

        DownloadTask existing = tasks.get(avid);
        if (existing != null &&
            (existing.getStatus() == DownloadStatus.PENDING ||
             existing.getStatus() == DownloadStatus.DOWNLOADING)) {
            logger.info("[{}] すでに処理中", avid);
            return existing;
        }

        DownloadTask task = new DownloadTask(avid);
        tasks.put(avid, task);

        executor.submit(() -> runDownload(finalAvid, task));
        return task;
    }

    /** 実際のダウンロード処理 (別スレッドで実行) */
    private void runDownload(String avid, DownloadTask task) {
        try {
            // --- キャンセルチェック ---
            if (task.isCancelRequested()) {
                task.setStatus(DownloadStatus.CANCELLED);
                return;
            }

            // 1. 元データ取得
            task.setStatus(DownloadStatus.DOWNLOADING);
            Optional<AVDownloadInfo> infoOpt = downloader.downloadInfo(avid);
            if (infoOpt.isEmpty()) {
                task.setStatus(DownloadStatus.FAILED);
                task.setErrorMessage("元データの取得に失敗しました");
                return;
            }

            AVDownloadInfo info = infoOpt.get();
            task.setInfo(info);

            // --- キャンセルチェック ---
            if (task.isCancelRequested()) {
                task.setStatus(DownloadStatus.CANCELLED);
                return;
            }

            // 2. m3u8 → mp4 ダウンロード
            boolean success = downloader.downloadM3u8(info.m3u8, avid);
            if (!success) {
                task.setStatus(DownloadStatus.FAILED);
                task.setErrorMessage("m3u8 のダウンロードに失敗しました");
                return;
            }

            task.setStatus(DownloadStatus.DONE);
            logger.info("[{}] ダウンロード完了", avid);

        } catch (Exception e) {
            logger.error("[{}] 予期しないエラー: {}", avid, e.getMessage());
            task.setStatus(DownloadStatus.FAILED);
            task.setErrorMessage(e.getMessage());
        }
    }

    // ----------------------------------------------------------
    // GET /info/{avid}
    // ----------------------------------------------------------

    /**
     * 保存済みの元データを返す。
     * タスクが存在しない、または info 未取得の場合は empty。
     */
    public Optional<AVDownloadInfo> getInfo(String avid) {
        avid = avid.toUpperCase();
        DownloadTask task = tasks.get(avid);
        if (task == null || task.getInfo() == null) {
            return Optional.empty();
        }
        return Optional.of(task.getInfo());
    }

    // ----------------------------------------------------------
    // GET /status/{avid}
    // ----------------------------------------------------------

    /** タスク状態を返す */
    public Optional<DownloadTask> getStatus(String avid) {
        return Optional.ofNullable(tasks.get(avid.toUpperCase()));
    }

    // ----------------------------------------------------------
    // DELETE /cancel/{avid}
    // ----------------------------------------------------------

    /**
     * ダウンロードをキャンセルする。
     * すでに完了 / 失敗 / キャンセル済みの場合は false を返す。
     */
    public boolean cancel(String avid) {
        avid = avid.toUpperCase();
        DownloadTask task = tasks.get(avid);
        if (task == null) return false;

        DownloadStatus s = task.getStatus();
        if (s == DownloadStatus.DONE ||
            s == DownloadStatus.FAILED ||
            s == DownloadStatus.CANCELLED) {
            return false;
        }

        task.requestCancel();
        task.setStatus(DownloadStatus.CANCELLED);
        logger.info("[{}] キャンセルリクエスト受付", avid);
        return true;
    }
}
