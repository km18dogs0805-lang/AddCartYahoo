package com.example.samplewebooooo.config;

import com.example.samplewebooooo.config.Downloader;
import com.example.samplewebooooo.config.AVDownloadInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

/**
 * Downloader の Bean 定義。
 * application.properties で設定値を注入する。
 *
 * downloader.path=
 * downloader.proxy=
 * downloader.timeout=15
 * downloader.domain=
 */
@Configuration
public class DownloaderConfig {

    @Value("${downloader.path:/tmp/downloader}")
    private String path;

    @Value("${downloader.proxy:#{null}}")
    private String proxy;

    @Value("${downloader.timeout:15}")
    private int timeout;

    @Value("${downloader.domain:}")
    private String domain;

    /**
     * サブクラスを差し替えるだけで別サイトに対応できる。
     * ここでは匿名クラスでスタブ実装を提供する。
     * 実際は MissavDownloader 等の具体クラスを返すこと。
     */
    @Bean
    public Downloader downloader() {
        Downloader d = new Downloader(path, proxy, timeout) {

            @Override
            public String getDownloaderName() {
                return "DefaultDownloader";
            }

            @Override
            public Optional<String> getHTML(String avid) {
                // TODO: 具体的なサイト向け実装に差し替える
                return fetchHTML("https://" + domain + "/" + avid.toLowerCase());
            }

            @Override
            public Optional<AVDownloadInfo> parseHTML(String html, String avid) {
                // TODO: HTML パース実装に差し替える
                AVDownloadInfo info = new AVDownloadInfo();
                info.avid  = avid;
                info.title = "タイトル未実装";
                info.m3u8  = "";
                return Optional.of(info);
            }
        };

        d.setDomain(domain);
        return d;
    }
}
