package com.example.samplewebooooo.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Global configuration: HTTP headers, mirror lists, Cloudflare overrides, UI prefs.
 * Mirrors Python's config.py
 */
public class Config {

    // ── HTTP User-Agent ───────────────────────────────────────────────────────
    public static final Map<String, String> DEFAULT_HEADERS = Map.of(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
        + "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
    );

    // ── Mirror lists per site key ─────────────────────────────────────────────
    public static final Map<String, List<String>> MIRRORS = Map.of(
        "missav", List.of("missav.ai", "missav.ws", "missav123.com", "missav.live"),
        "jable",  List.of("jable.tv", "fs1.app"),
        "supjav", List.of("supjav.com")
    );

    // ── Cloudflare override store ─────────────────────────────────────────────
    private static final ReentrantLock cfLock    = new ReentrantLock();
    private static final ReentrantLock prefsLock = new ReentrantLock();

    /** host → {cookie?, ua?} */
    private static volatile Map<String, Map<String, String>> CF_OVERRIDES = new HashMap<>();

    private static final ObjectMapper JSON = new ObjectMapper();

    // ── Storage paths ─────────────────────────────────────────────────────────
    public static Path cfStorePath() {
        String base = System.getenv("APPDATA");
        if (base == null || base.isBlank()) base = System.getProperty("user.home");
        return Path.of(base, "JableTV Downloader", "cf_overrides.json");
    }

    public static Path uiPrefsPath() {
        return cfStorePath().resolveSibling("ui_prefs.json");
    }

    public static Path queueCsvPath() {
        return uiPrefsPath().resolveSibling("download_queue.csv");
    }

    // ── Theme & language ──────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadPrefs() {
        try {
            return JSON.readValue(uiPrefsPath().toFile(), Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private static void savePrefs(Map<String, Object> prefs) {
        Path p = uiPrefsPath();
        try {
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
            JSON.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), prefs);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ignored) {}
    }

    public static String getTheme() {
        Object v = loadPrefs().get("theme");
        if (v instanceof String s) {
            s = s.strip().toLowerCase();
            if (Set.of("system", "light", "dark").contains(s)) return s;
        }
        return "system";
    }

    public static void setTheme(String mode) {
        mode = (mode == null ? "" : mode).strip().toLowerCase();
        if (!Set.of("system", "light", "dark").contains(mode)) mode = "system";
        prefsLock.lock();
        try {
            Map<String, Object> prefs = new HashMap<>(loadPrefs());
            prefs.put("theme", mode);
            savePrefs(prefs);
        } finally { prefsLock.unlock(); }
    }

    public static String getUiLang() {
        Object v = loadPrefs().get("lang");
        if (v instanceof String s) {
            s = s.strip();
            if (Set.of("en", "zh", "zh-Hans", "ja").contains(s)) return s;
        }
        return null;
    }

    public static void setUiLang(String code) {
        code = (code == null ? "" : code).strip();
        if (!Set.of("en", "zh", "zh-Hans", "ja").contains(code)) code = "en";
        prefsLock.lock();
        try {
            Map<String, Object> prefs = new HashMap<>(loadPrefs());
            prefs.put("lang", code);
            savePrefs(prefs);
        } finally { prefsLock.unlock(); }
    }

    // ── Cloudflare overrides ──────────────────────────────────────────────────
    private static String normHost(String host) {
        if (host == null) return "";
        host = host.strip().toLowerCase().replaceAll("\\.$", "");
        if (host.contains(":")) host = host.split(":", 2)[0].replaceAll("\\.$", "");
        return host;
    }

    private static String parseCfClearance(String raw) {
        if (raw == null) return "";
        raw = raw.strip().replaceAll("[\\x00-\\x1f\\x7f]+", "");
        if (raw.contains("cf_clearance=")) {
            var m = java.util.regex.Pattern.compile("cf_clearance=([^;,\\s]+)").matcher(raw);
            return m.find() ? m.group(1) : "";
        }
        return raw.replaceAll("^['\"]|['\"]$", "");
    }

    public static Map<String, String> getCfOverride(String host) {
        cfLock.lock();
        try {
            var entry = CF_OVERRIDES.get(normHost(host));
            return entry != null ? new HashMap<>(entry) : null;
        } finally { cfLock.unlock(); }
    }

    public static List<String> cfOverrideHosts() {
        cfLock.lock();
        try {
            List<String> list = new ArrayList<>(CF_OVERRIDES.keySet());
            Collections.sort(list);
            return list;
        } finally { cfLock.unlock(); }
    }

    public static void setCfOverride(String host, String cookie, String ua) {
        String h = normHost(host);
        if (h.isBlank()) return;
        Map<String, String> entry = new HashMap<>();
        String ck = parseCfClearance(cookie);
        if (!ck.isBlank()) entry.put("cookie", ck);
        if (ua != null && !ua.isBlank()) entry.put("ua", ua.strip());

        cfLock.lock();
        try {
            Map<String, Map<String, String>> next = new HashMap<>(CF_OVERRIDES);
            if (!entry.isEmpty()) next.put(h, entry);
            else next.remove(h);
            CF_OVERRIDES = next;
        } finally { cfLock.unlock(); }
        saveCfOverrides();
    }

    public static void clearCfOverride(String host) {
        setCfOverride(host, "", "");
    }

    @SuppressWarnings("unchecked")
    public static void loadCfOverrides() {
        Path p = cfStorePath();
        if (!Files.exists(p)) {
            cfLock.lock(); try { CF_OVERRIDES = new HashMap<>(); } finally { cfLock.unlock(); }
            return;
        }
        try {
            Map<String, Object> raw = JSON.readValue(p.toFile(), Map.class);
            Map<String, Map<String, String>> parsed = new HashMap<>();
            for (var e : raw.entrySet()) {
                String h = normHost(e.getKey());
                if (h.isBlank() || !(e.getValue() instanceof Map<?,?> m)) continue;
                Map<String, String> clean = new HashMap<>();
                Object ckObj = m.get("cookie");
                if (ckObj instanceof String ck) {
                    ck = parseCfClearance(ck);
                    if (!ck.isBlank()) clean.put("cookie", ck);
                }
                Object uaObj = m.get("ua");
                if (uaObj instanceof String ua && !ua.isBlank()) clean.put("ua", ua.strip());
                if (!clean.isEmpty()) parsed.put(h, clean);
            }
            cfLock.lock(); try { CF_OVERRIDES = parsed; } finally { cfLock.unlock(); }
        } catch (Exception ex) {
            cfLock.lock(); try { CF_OVERRIDES = new HashMap<>(); } finally { cfLock.unlock(); }
        }
    }

    public static void saveCfOverrides() {
        cfLock.lock();
        Map<String, Map<String, String>> snapshot;
        try { snapshot = new HashMap<>(CF_OVERRIDES); } finally { cfLock.unlock(); }
        Path p = cfStorePath();
        try {
            Files.createDirectories(p.getParent());
            Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
            JSON.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), snapshot);
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception ignored) {}
    }

    static { try { loadCfOverrides(); } catch (Exception ignored) {} }
}
