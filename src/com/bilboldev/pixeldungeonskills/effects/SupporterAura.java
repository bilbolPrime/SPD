package com.bilboldev.pixeldungeonskills.effects;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.particles.Emitter;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.ui.SupporterIcon;
import com.bilboldev.utils.RectF;
import java.util.LinkedHashMap;

public final class SupporterAura extends Emitter {
    private static final Object SHIMMER_TEXTURE = SupporterAura.class;
    private static final int PAD = 12;
    private final Image hero;
    private final Light halo = new Light();
    private final Light edge = new Light();
    private final Light[] wisps = new Light[18];
    private final LinkedHashMap<Integer, SmartTexture> glows = new LinkedHashMap<>(32, .75f, true);
    private SmartTexture source;
    private int style;
    private double phase;

    public SupporterAura(Image hero) {
        this.hero = hero;
        renderAnchor(hero);
        autoKill = false;
        lightMode = true;
        add(halo);
        add(edge);
        for (int i = 0; i < wisps.length; i++) {
            wisps[i] = new Light();
            wisps[i].texture(shimmerTexture());
            wisps[i].origin.set(16, 16);
            add(wisps[i]);
        }
    }

    public void style(int style) {
        this.style = style;
        visible = style > 0;
    }

    @Override
    public void update() {
        if (!hero.exists || hero.parent == null) {
            killAndErase();
            destroy();
            return;
        }
        visible = style > 0 && hero.visible;
        if (hero instanceof CharSprite) {
            CharSprite sprite = (CharSprite)hero;
            visible &= sprite.ch != null && sprite.ch.isAlive() && sprite.ch.invisible == 0;
        }
        if (!visible) return;

        phase += Game.elapsed;
        float breath = 1 + .06f * (float)Math.sin(phase * 1.8);
        float scale = hero.height() / 15f;
        float centerX = hero.x + hero.width() / 2;
        float centerY = hero.y + hero.height() / 2;
        int color = SupporterIcon.color(style, (float)phase);
        SmartTexture glow = glowTexture();
        halo.pose(glow, style, hero);
        halo.hardlight(color);
        halo.alpha((.17f + style * .09f) * breath * hero.am);
        edge.pose(glow, 0, hero);
        edge.hardlight(0xFFF5DF);
        edge.alpha((.12f + style * .035f) * breath * hero.am);

        int count = 8 + style * 2;
        for (int i = 0; i < wisps.length; i++) {
            Light wisp = wisps[i];
            wisp.visible = i < count;
            if (!wisp.visible) continue;
            float angle = (i + .12f * (float)Math.sin(i * 2.4)) * 6.283185f / count;
            float shimmer = .5f + .5f * (float)Math.sin(phase * (1.6 + i % 5 * .13) + i * 2.4);
            float length = 2.2f + style * .45f + shimmer;
            float dx = (float)Math.cos(angle);
            float dy = (float)Math.sin(angle);
            float radius = 1 / (float)Math.sqrt(dx * dx / 20.25f + dy * dy / 46.24f);
            wisp.scale.set(scale * (2.4f + style * .3f) / 32, scale * length / 32);
            wisp.x = centerX + dx * (radius + shimmer * .5f) * scale - wisp.origin.x;
            wisp.y = centerY + dy * (radius + shimmer * .5f) * scale - wisp.origin.y;
            wisp.angle = angle * 57.29578f + 90;
            wisp.hardlight(i % 3 == 0 ? 0xFFF5DF : color);
            wisp.alpha((.08f + style * .025f) * (.3f + shimmer * .7f) * hero.am);
        }
    }

    private SmartTexture glowTexture() {
        if (source != hero.texture) {
            clearGlows();
            source = hero.texture;
        }
        RectF frame = hero.frame();
        int left = Math.round(frame.left * source.width);
        int top = Math.round(frame.top * source.height);
        int key = top * source.width + left;
        SmartTexture glow = glows.get(key);
        if (glow != null) return glow;

        int width = Math.round(hero.width);
        int height = Math.round(hero.height);
        int[] sprite = new int[width * height];
        source.bitmap.getPixels(sprite, 0, width, left, top, width, height);
        int paddedWidth = width + PAD * 2;
        int paddedHeight = height + PAD * 2;
        int area = paddedWidth * paddedHeight;
        int[] pixels = new int[area * 6];
        for (int y = 0; y < paddedHeight; y++) {
            for (int x = 0; x < paddedWidth; x++) {
                int distance = PAD * PAD * 2;
                for (int sy = 0; sy < height; sy++) {
                    for (int sx = 0; sx < width; sx++) {
                        if ((sprite[sx + sy * width] >>> 24) < 128) continue;
                        int dx = x - PAD - sx;
                        int dy = y - PAD - sy;
                        distance = Math.min(distance, dx * dx + dy * dy);
                    }
                }
                for (int layer = 0; layer <= 5; layer++) {
                    double spread = layer == 0 ? .85 : 1 + layer * .55;
                    double alpha = distance == 0 ? (layer == 0 ? .12 : 0)
                            : Math.exp(-distance / (2 * spread * spread));
                    pixels[x + y * paddedWidth + layer * area] = ((int)(255 * alpha) << 24) | 0xFFFFFF;
                }
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(paddedWidth, paddedHeight * 6, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, paddedWidth, 0, 0, paddedWidth, paddedHeight * 6);
        glow = new SmartTexture(bitmap, SmartTexture.LINEAR, SmartTexture.CLAMP, false);
        TextureCache.add(glow, glow);
        glows.put(key, glow);
        if (glows.size() > 32) {
            Integer oldest = glows.keySet().iterator().next();
            TextureCache.remove(glows.remove(oldest));
        }
        return glow;
    }

    private void clearGlows() {
        for (SmartTexture glow : glows.values()) TextureCache.remove(glow);
        glows.clear();
    }

    @Override
    public void destroy() {
        clearGlows();
        super.destroy();
    }

    public static final class Light extends Speck {
        private int layer = -1;

        public boolean followsBody() {
            return layer >= 0;
        }

        void pose(SmartTexture glow, int next, Image hero) {
            boolean changed = texture != glow || layer != next || flipHorizontal != hero.flipHorizontal;
            texture = glow;
            layer = next;
            flipHorizontal = hero.flipHorizontal;
            if (changed) {
                frame(0, next * glow.height / 6, glow.width, glow.height / 6);
                origin.set(width / 2, height / 2);
            }
            scale.set(hero.scale);
            x = hero.x + hero.width() / 2 - origin.x;
            y = hero.y + hero.height() / 2 - origin.y;
        }

        @Override public void update() { }
    }

    private static SmartTexture shimmerTexture() {
        if (!TextureCache.contains(SHIMMER_TEXTURE)) {
            int[] pixels = new int[32 * 32];
            for (int y = 0; y < 32; y++) {
                double dy = (y - 15.5) / 15.5;
                for (int x = 0; x < 32; x++) {
                    double dx = (x - 15.5) / 15.5;
                    double alpha = Math.max(0, Math.exp(-4.5 * (dx * dx + dy * dy)) - .011);
                    pixels[x + y * 32] = ((int)(255 * alpha) << 24) | 0xFFFFFF;
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 32, 0, 0, 32, 32);
            TextureCache.add(SHIMMER_TEXTURE, new SmartTexture(bitmap, SmartTexture.LINEAR, SmartTexture.CLAMP, false));
        }
        return TextureCache.get(SHIMMER_TEXTURE);
    }
}
