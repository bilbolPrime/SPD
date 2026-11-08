package com.bilboldev.pixeldungeonskills.desktop;

import com.bilboldev.noosa.Game;
import com.bilboldev.pixeldungeonskills.GameUpdates;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class GitHubGameUpdates extends GameUpdates {
    private static final String RELEASES = "https://github.com/bilbolPrime/SPD/releases";
    private static final String API = "https://api.github.com/repos/bilbolPrime/SPD/releases/latest";
    private String etag;

    public GitHubGameUpdates() {
        super("github", RELEASES + "/latest");
    }

    @Override
    protected void fetch() {
        final String version = Game.version;
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection)new URL(API).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "Skillful-Pixel-Dungeon");
            connection.setRequestProperty("Accept", "application/vnd.github+json");
            if (etag != null) connection.setRequestProperty("If-None-Match", etag);
            int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_NOT_MODIFIED) return;
            if (status != HttpURLConnection.HTTP_OK) return;
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                long started = System.nanoTime();
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (body.size() + count > 256 * 1024
                            || System.nanoTime() - started > 15_000_000_000L) return;
                    body.write(buffer, 0, count);
                }
            }
            JSONObject release = new JSONObject(new String(body.toByteArray(), StandardCharsets.UTF_8));
            available = hasUpdate(release, version, System.getProperty("os.name", ""));
            etag = connection.getHeaderField("ETag");
        } catch (IOException | JSONException unavailable) {

        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    static boolean hasUpdate(JSONObject release, String installed, String os) {
        if (release.optBoolean("draft", true) || release.optBoolean("prerelease", true)) return false;
        if (!newer(release.optString("tag_name"), installed)) return false;
        JSONArray assets = release.optJSONArray("assets");
        if (assets == null) return false;
        os = os.toLowerCase(Locale.ROOT);
        String suffix = os.contains("windows") ? "-windows.zip"
                : os.contains("mac") ? "-macos.zip" : os.contains("linux") ? "-linux.tar.gz" : "";
        if (suffix.isEmpty()) return false;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null) continue;
            String name = asset.optString("name");
            if (name.startsWith("skillful-pixel-dungeon-free-") && name.endsWith(suffix)
                    && "uploaded".equals(asset.optString("state"))
                    && asset.optString("browser_download_url").startsWith(RELEASES + "/download/")) return true;
        }
        return false;
    }

    static boolean newer(String release, String installed) {
        if (release == null || installed == null) return false;
        String pattern = "[vV]?[0-9]{1,8}(\\.[0-9]{1,8}){1,3}";
        if (!release.matches(pattern) || !installed.matches(pattern)) return false;
        String[] next = release.replaceFirst("^[vV]", "").split("\\.");
        String[] current = installed.replaceFirst("^[vV]", "").split("\\.");
        for (int i = 0; i < Math.max(next.length, current.length); i++) {
            int a = i < next.length ? Integer.parseInt(next[i]) : 0;
            int b = i < current.length ? Integer.parseInt(current[i]) : 0;
            if (a != b) return a > b;
        }
        return false;
    }
}
