package com.example.samplewebooooo.model;

import com.example.samplewebooooo.config.AVDownloadInfo;
import java.time.LocalDateTime;

/**
 * avid ごとのダウンロードタスク状態を保持するモデル
 */
public class DownloadTask {

    private final String avid;
    private DownloadStatus status;
    private AVDownloadInfo info;
    private String errorMessage;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** キャンセル用フラグ (downloadM3u8 実行前に参照) */
    private volatile boolean cancelRequested = false;

    public DownloadTask(String avid) {
        this.avid      = avid;
        this.status    = DownloadStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // ---- ゲッター ----

    public String getAvid()             { return avid; }
    public DownloadStatus getStatus()   { return status; }
    public AVDownloadInfo getInfo()     { return info; }
    public String getErrorMessage()     { return errorMessage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public boolean isCancelRequested()  { return cancelRequested; }

    // ---- セッター (状態遷移) ----

    public void setStatus(DownloadStatus status) {
        this.status    = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void setInfo(AVDownloadInfo info)         { this.info = info; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void requestCancel()                      { this.cancelRequested = true; }
}
