package com.bilboldev.pixeldungeonskills.windows;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.BitmapTextMultiline;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.ui.Component;
import com.bilboldev.pixeldungeonskills.Assets;
import com.bilboldev.pixeldungeonskills.Chrome;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.scenes.PixelScene;
import com.bilboldev.pixeldungeonskills.sprites.GhostSprite;
import com.bilboldev.pixeldungeonskills.sprites.WandmakerSprite;
import com.bilboldev.pixeldungeonskills.ui.HighlightedText;
import com.bilboldev.pixeldungeonskills.ui.RebornChrome;
import com.bilboldev.pixeldungeonskills.ui.ScrollPane;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WndUpdateHistory extends WndScrollable {
    private static final int INK = 0xE5DDD1;
    private static final int GOLD = 0xD9B77D;
    private static final int TEXT_X = 22;

    private BitmapText backToTop;

    public WndUpdateHistory() {
        int lastRead = Presentation.lastReadUpdate();
        int width = boundedWidth(landscapeLayout() ? 320 : 220);
        Component heading = new Component();
        Image scroll = new Image(Assets.CHROME, 32, 32, 32, 32);
        scroll.scale.set(.75f);
        scroll.x = 3;
        scroll.y = 2;
        heading.add(scroll);
        BitmapTextMultiline title = PixelScene.createMultiline(width < 190 ? 11 : 14);
        title.text("Update History", false);
        title.maxWidth = width - 34;
        title.measure();
        title.hardlight(GOLD);
        title.x = 34;
        title.y = 6;
        heading.add(title);
        float ruleY = Math.max(31, title.y + title.height() + 6);
        rule(heading, 0, ruleY, width, 1, 0xFF625269);
        heading.setSize(width, ruleY + 7);

        Component content = new Component();
        int bodyWidth = width - SCROLL_GUTTER;
        float y = 2;
        try {
            JSONArray entries = readEntries();
            for (int i = entries.length() - 1; i >= 0; i--) {
                JSONObject entry = entries.getJSONObject(i);
                if (entry.has("version")) {
                    y = release(content, entry, bodyWidth, y, lastRead);
                } else {
                    y = archive(content, entry, bodyWidth, y);
                }
            }
        } catch (IOException | JSONException failure) {
            throw new IllegalStateException("Cannot read update history", failure);
        }
        backToTop = PixelScene.createText(9);
        backToTop.text("Back to top", false);
        backToTop.measure();
        backToTop.hardlight(GOLD);
        backToTop.x = PixelScene.align((bodyWidth - backToTop.width()) / 2);
        backToTop.y = y + 8;
        content.add(backToTop);
        content.setSize(bodyWidth, backToTop.y + backToTop.height() + 8);
        layoutContent(width, heading, content, 4);
        Presentation.lastReadUpdate(Math.max(lastRead, Game.versionBuild));
    }

    @Override
    protected RebornChrome.Material rebornMaterial() {
        return RebornChrome.Material.ARCANE;
    }

    @Override
    protected ScrollPane createScrollPane(Component content) {
        return new ScrollPane(content) {
            @Override
            public void onClick(float x, float y) {
                if (x >= backToTop.x - 8 && x <= backToTop.x + backToTop.width() + 8
                        && y >= backToTop.y - 8 && y <= backToTop.y + backToTop.height() + 8) {
                    scrollTo(0, 0);
                }
            }
        };
    }

    private static JSONArray readEntries() throws IOException, JSONException {
        try (Reader reader = new InputStreamReader(Game.instance.getAssets().open("update-history.json"),
                StandardCharsets.UTF_8)) {
            StringBuilder text = new StringBuilder();
            char[] buffer = new char[2048];
            int count;
            while ((count = reader.read(buffer)) != -1) text.append(buffer, 0, count);
            return new JSONArray(text.toString());
        }
    }

    private static float release(Component content, JSONObject entry, int width, float y, int lastRead)
            throws JSONException {
        boolean compact = width < 170;
        int textX = compact ? 4 : TEXT_X;
        String label = entry.getString("version");
        if (entry.getInt("build") > lastRead) label += " - _New_";
        HighlightedText title = paragraph(content, label, 9, GOLD, textX, y, width - textX - 5);
        title.setColor(GOLD, 0xFFFF88);
        float top = y;
        String buildLabel = "Build " + entry.getInt("build");
        if (entry.has("builds")) {
            JSONArray builds = entry.getJSONArray("builds");
            StringBuilder grouped = new StringBuilder("Builds ");
            for (int i = 0; i < builds.length(); i++) {
                if (i > 0) grouped.append(", ");
                grouped.append(builds.getInt(i));
            }
            buildLabel = grouped.toString();
        }
        HighlightedText metadata = paragraph(content, buildLabel + " - " + entry.getString("date"),
                7, 0xB4A7C2, textX, title.bottom() + 4, width - textX - 5);
        y = metadata.bottom() + 8;
        JSONArray notes = entry.getJSONArray("notes");
        for (int i = 0; i < notes.length(); i++) {
            rule(content, textX + 1, y + 3, 2, 2, 0xFF9A839E);
            HighlightedText note = paragraph(content, notes.getString(i), 7, INK,
                    textX + 8, y, width - textX - 12);
            y += note.height() + 5;
        }
        if (!compact) {
            rule(content, 9, top + 8, 1, y - top - 3, 0xFF514557);
            rule(content, 7, top + 2, 5, 5, 0xFF9A7A51);
            rule(content, 8, top + 3, 3, 3, 0xFFD9B77D);
        }
        rule(content, textX, y + 3, width - textX - 5, 1, 0xFF39323F);
        return y + 19;
    }

    private static float archive(Component content, JSONObject entry, int width, float y) throws JSONException {
        boolean compact = width < 170;
        int textX = compact ? 10 : 64;
        int textWidth = width - textX - 12;
        HighlightedText title = new HighlightedText(10);
        title.setColor(GOLD, GOLD);
        title.text(entry.getString("title"), textWidth, false);
        HighlightedText caption = new HighlightedText(7);
        caption.setColor(0xB4A7C2, GOLD);
        caption.text(entry.getString("caption"), textWidth, false);
        float height = compact ? 74 + title.height() + caption.height()
                : Math.max(68, title.height() + caption.height() + 26);
        RebornChrome frame = new RebornChrome(Chrome.Type.BUTTON).material(RebornChrome.Material.ARCANE);
        frame.x = 3;
        frame.y = y;
        frame.size(width - 6, height);
        content.add(frame);
        float middle = y + (compact ? 29 : height / 2);
        float artX = compact ? (width - 60) / 2f : 0;
        if ("ghosts".equals(entry.getString("art"))) {
            picture(content, new GhostSprite(), artX + 10, middle - 15, 1, .4f);
            picture(content, new GhostSprite(), artX + 43, middle - 9, 1, .55f);
            picture(content, new GhostSprite(), artX + 21, middle - 16, 2, .85f);
        } else {
            picture(content, new WandmakerSprite(), artX + 22, middle - 14, 2, 1);
        }
        title.setPos(textX, y + (compact ? 58 : (height - title.height() - caption.height() - 6) / 2));
        caption.setPos(textX, title.bottom() + 6);
        content.add(title);
        content.add(caption);
        return y + height + 20;
    }

    private static HighlightedText paragraph(Component parent, String value, int size, int color,
                                             float x, float y, int width) {
        HighlightedText text = new HighlightedText(size);
        text.setColor(color, GOLD);
        text.text(value, width, false);
        text.setPos(x, y);
        parent.add(text);
        return text;
    }

    private static void picture(Component parent, Image image, float x, float y, float scale, float alpha) {
        image.scale.set(scale);
        image.x = x;
        image.y = y;
        image.alpha(alpha);
        parent.add(image);
    }

    private static void rule(Component parent, float x, float y, float width, float height, int color) {
        ColorBlock line = new ColorBlock(width, height, color);
        line.x = x;
        line.y = y;
        parent.add(line);
    }

}
