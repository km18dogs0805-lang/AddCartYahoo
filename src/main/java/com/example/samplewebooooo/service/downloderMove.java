package com.example.samplewebooooo.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class downloderMove {
    /**
     * yt-dlpを実行するメソッド
     * @param url
     * @param outputDir
     * @return
     * @throws Exception
     */
    public List<String> downloadVideo(String url, String outputDir) throws Exception {
        
        // exeファイルを実行
        File ytDlpExe = new ClassPathResource("tools/yt-dlp.exe").getFile();
        String ytDlpPath = ytDlpExe.getAbsolutePath();

        String outDir = System.getProperty("java.io.tmpdir") + File.separator +"/downloads";
        System.out.println(outDir);
        
        Path dirPath = Paths.get(outDir);

        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath); // フォルダが無ければ自動作成
        }

        // archivePathを作成
        

        List<String> output = new ArrayList<>();
        // yt-dlpを外部コマンドから実行ProcessBuilder
        ProcessBuilder builder = new ProcessBuilder(
                ytDlpPath,
                "-f", "bestvideo[height<=720]+bestaudio/best[height<=720]",
                "--merge-output-format", "mp4",
                //"--extractor-args", "youtube:player_client=android",
                "--extractor-args", "generic:impersonate",
                "--rm-cache-dir",
                "--no-cookies",
                "-o", outputDir + "/%(title)s.%(ext)s", 
                url
        );
        // 標準エラー
        builder.redirectErrorStream(true);
        
        // 標準出力
        Process process = builder.start();

        // ファイルを読み書き
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), "MS932"))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                
                output.add(line);

            }

        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            System.out.println("=== yt-dlp output===");
            
            throw new RuntimeException("yt-dlp failed with exit code " + exitCode + "\n" + String.join("\n", output));

        }

        // DownloadHistoryを用いて、履歴として保存

        return output;
    
    }
}
