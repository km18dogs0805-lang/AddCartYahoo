package com.example.samplewebooooo.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandRunner {
    private static final Logger logger = LoggerFactory.getLogger(CommandRunner.class);

    private CommandRunner() {}

    /**
     * sh -c でコマンドを実行し、終了コード 0 なら true を返す。
     *
     * @param command 実行するコマンド文字列
     * @return 成功なら true
     */
    public static boolean run(String command) {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            logger.error("コマンド実行失敗: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
