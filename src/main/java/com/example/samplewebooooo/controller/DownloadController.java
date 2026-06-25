package com.example.samplewebooooo.controller;

import com.example.samplewebooooo.config.AVDownloadInfo;
import com.example.samplewebooooo.model.ApiResponse;
import com.example.samplewebooooo.model.DownloadTask;
import com.example.samplewebooooo.service.DownloadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ダウンロード API コントローラー
 *
 * POST   /api/download/{avid}  - ダウンロード開始
 * GET    /api/info/{avid}      - 元データ取得
 * GET    /api/status/{avid}    - ダウンロード状況確認
 * DELETE /api/cancel/{avid}    - キャンセル
 */
@RestController
@RequestMapping("/api")
public class DownloadController {

    private final DownloadService downloadService;

    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    // ----------------------------------------------------------
    // POST /api/download/{avid}
    // ----------------------------------------------------------

    /**
     * ダウンロードを開始する。
     * 処理は非同期なので、即座に PENDING / DOWNLOADING 状態が返る。
     *
     * 例: POST /api/download/ABC-123
     */
    @PostMapping("/download/{avid}")
    public ResponseEntity<ApiResponse<DownloadTask>> startDownload(@PathVariable String avid) {
        DownloadTask task = downloadService.startDownload(avid);
        return ResponseEntity
            .accepted()  // 202 Accepted
            .body(ApiResponse.ok("ダウンロードを受け付けました", task));
    }

    // ----------------------------------------------------------
    // GET /api/info/{avid}
    // ----------------------------------------------------------

    /**
     * 取得済みの元データ (AVDownloadInfo) を返す。
     * まだダウンロードを開始していない、または元データ未取得の場合は 404。
     *
     * 例: GET /api/info/ABC-123
     */
    @GetMapping("/info/{avid}")
    public ResponseEntity<ApiResponse<AVDownloadInfo>> getInfo(@PathVariable String avid) {
        return downloadService.getInfo(avid)
            .map(info -> ResponseEntity.ok(ApiResponse.ok("元データを取得しました", info)))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("元データが見つかりません: " + avid)));
    }

    // ----------------------------------------------------------
    // GET /api/status/{avid}
    // ----------------------------------------------------------

    /**
     * ダウンロードの進行状況を返す。
     * タスクが存在しない場合は 404。
     *
     * 例: GET /api/status/ABC-123
     */
    @GetMapping("/status/{avid}")
    public ResponseEntity<ApiResponse<DownloadTask>> getStatus(@PathVariable String avid) {
        return downloadService.getStatus(avid)
            .map(task -> ResponseEntity.ok(ApiResponse.ok("ステータスを取得しました", task)))
            .orElseGet(() -> ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("タスクが見つかりません: " + avid)));
    }

    // ----------------------------------------------------------
    // DELETE /api/cancel/{avid}
    // ----------------------------------------------------------

    /**
     * ダウンロードをキャンセルする。
     * すでに完了・失敗・キャンセル済みの場合は 409 Conflict。
     *
     * 例: DELETE /api/cancel/ABC-123
     */
    @DeleteMapping("/cancel/{avid}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable String avid) {
        boolean cancelled = downloadService.cancel(avid);
        if (cancelled) {
            return ResponseEntity.ok(ApiResponse.ok("キャンセルしました", null));
        }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error("キャンセルできません (完了済み・未存在): " + avid));
    }
}
