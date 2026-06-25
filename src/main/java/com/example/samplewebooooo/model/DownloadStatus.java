package com.example.samplewebooooo.model;

/**
 * ダウンロードの状態
 */
public enum DownloadStatus {
    PENDING,      // キュー待ち
    DOWNLOADING,  // ダウンロード中
    DONE,         // 完了
    FAILED,       // 失敗
    CANCELLED     // キャンセル済み
}
