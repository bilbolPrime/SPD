package com.bilboldev.pixeldungeonskills.effects;

import android.graphics.Bitmap;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Image;
import com.bilboldev.noosa.MovieClip;
import com.bilboldev.pixeldungeonskills.CosmeticAccessories;
import com.bilboldev.pixeldungeonskills.actors.hero.HeroClass;
import com.bilboldev.utils.RectF;

public final class CosmeticAccessory extends Image {
    public static final int SIZE = 24;
    public static final int ANCHOR_X = 6;
    public static final int ANCHOR_Y = 9;
    private static final int FRAMES = 21;
    private static final int TIERS = 7;
    private static final int MAX_UNUSED_PIXELS = 512 * 1024;
    private static final Overlay[][] overlays = new Overlay[HeroClass.values().length][CosmeticAccessories.COUNT];
    private static long cacheClock;
    private final MovieClip owner;
    private HeroClass heroClass;
    private int style;
    private float age;
    private Overlay overlay;
    private RectF displayed;

    public CosmeticAccessory(MovieClip owner, HeroClass heroClass) {
        this.owner = owner;
        this.heroClass = heroClass;
        visible = false;
    }

    public void heroClass(HeroClass heroClass) {
        if (this.heroClass == heroClass) return;
        releaseOverlay();
        this.heroClass = heroClass;
    }

    public void style(int style) {
        if (CosmeticAccessories.valid(style) != style) throw new IllegalArgumentException("Unknown accessory");
        if (this.style == style) return;
        releaseOverlay();
        this.style = style;
        age = 0;
        pose();
    }

    @Override public void update() {
        age += Math.max(0, Math.min(Game.elapsed, .1f));
        pose();
    }

    public void pose() {
        visible = exists && style > 0 && owner.exists && owner.visible && owner.texture != null;
        if (!visible) return;
        if (overlay == null || overlay.texture.bitmap == null) {
            releaseOverlay();
            overlay = overlays[heroClass.ordinal()][style - 1];
            if (overlay == null || overlay.texture.bitmap == null) {
                overlay = new Overlay(heroClass, style);
                overlays[heroClass.ordinal()][style - 1] = overlay;
            }
            overlay.references++;
            overlay.lastUsed = ++cacheClock;
            trimCache();
            texture(overlay.texture);
            displayed = null;
        }
        int frame = Math.min(FRAMES - 1, owner.frameLeft() / 12);
        int tier = Math.min(TIERS - 1, owner.frameTop() / 15);
        int spin = style == 6 && age % 6 >= 4.5f ? (int)(age * 12) % 4 : 0;
        RectF next = overlay.frames[(tier * overlay.spins + spin) * FRAMES + frame];
        boolean flipped = flipHorizontal != owner.flipHorizontal || flipVertical != owner.flipVertical;
        flipHorizontal = owner.flipHorizontal;
        flipVertical = owner.flipVertical;
        if (displayed != next || flipped) {
            frame(next);
            displayed = next;
        }
        scale.set(owner.scale);
        angle = owner.angle;
        origin.set(owner.origin.x + ANCHOR_X, owner.origin.y + ANCHOR_Y);
        x = owner.x - ANCHOR_X;
        y = owner.y - ANCHOR_Y;
        camera = owner.camera();
        rm = owner.rm;
        gm = owner.gm;
        bm = owner.bm;
        am = owner.am;
        ra = owner.ra;
        ga = owner.ga;
        ba = owner.ba;
        aa = owner.aa;
    }

    @Override public void draw() {
        pose();
        if (visible) super.draw();
    }

    @Override public void destroy() {
        visible = false;
        kill();
        releaseOverlay();
        super.destroy();
    }

    private void releaseOverlay() {
        if (overlay == null) return;
        overlay.references--;
        overlay.lastUsed = ++cacheClock;
        overlay = null;
        displayed = null;
        trimCache();
    }

    private static void trimCache() {
        int unused = 0;
        Overlay oldest = null;
        for (Overlay[] hero : overlays) {
            for (Overlay overlay : hero) {
                if (overlay == null || overlay.references > 0) continue;
                if (overlay.texture.bitmap == null) {
                    overlays[overlay.hero.ordinal()][overlay.style - 1] = null;
                    continue;
                }
                unused += overlay.texture.width * overlay.texture.height;
                if (oldest == null || overlay.lastUsed < oldest.lastUsed) oldest = overlay;
            }
        }
        if (unused > MAX_UNUSED_PIXELS && oldest != null) {
            overlays[oldest.hero.ordinal()][oldest.style - 1] = null;
            TextureCache.remove(oldest.key);
            trimCache();
        }
    }

    private static final class Overlay {
        final SmartTexture texture;
        final RectF[] frames;
        final int spins;
        final HeroClass hero;
        final int style;
        final Object key = new Object();
        int references;
        long lastUsed;

        Overlay(HeroClass hero, int style) {
            this.hero = hero;
            this.style = style;
            spins = style == 6 ? 4 : 1;
            Bitmap art = TextureCache.get("heroes/accessories/worn.png").bitmap;
            Bitmap rotor = style == 6 ? TextureCache.get("heroes/accessories/propeller.png").bitmap : null;
            Bitmap ground = TextureCache.get("heroes/accessories/ground.png").bitmap;
            Bitmap foreground = TextureCache.get("heroes/accessories/" + hero.name().toLowerCase(java.util.Locale.ROOT) + ".png").bitmap;
            Bitmap bitmap = Bitmap.createBitmap(FRAMES * SIZE, TIERS * spins * SIZE, Bitmap.Config.ARGB_8888);
            frames = new RectF[FRAMES * TIERS * spins];
            texture = new SmartTexture(bitmap);
            for (int tier = 0; tier < TIERS; tier++) {
                for (int spin = 0; spin < spins; spin++) {
                    int top = (tier * spins + spin) * SIZE;
                    for (int frame = 0; frame < FRAMES; frame++) {
                        int left = frame * SIZE;
                        frames[(tier * spins + spin) * FRAMES + frame] = texture.uvRect(left, top, left + SIZE, top + SIZE);
                        if (frame >= 8 && frame <= 12) {
                            for (int y = 0; y < 16; y++) {
                                for (int x = 0; x < 16; x++) {
                                    bitmap.setPixel(left + ANCHOR_X + x, top + 8 + y, ground.getPixel((style - 1) * 16 + x, y));
                                }
                            }
                            continue;
                        }
                        int pose = frame == 1 ? 1 : frame == 16 || frame == 17 || frame == 19 ? 2 : 0;
                        int dx = frame == 13 ? -1 : frame == 15 ? 1 : 0;
                        int dy = frame == 4 || frame == 7 ? 1 : 0;
                        for (int y = 0; y < SIZE; y++) {
                            for (int x = 0; x < SIZE; x++) {
                                int sourceX = x - dx, sourceY = y - dy;
                                if (sourceX < 0 || sourceX >= SIZE || sourceY < 0 || sourceY >= SIZE) continue;
                                int color = rotor == null ? art.getPixel(pose * SIZE + sourceX, (style - 1) * SIZE + sourceY)
                                        : rotor.getPixel(spin * SIZE + sourceX, pose * SIZE + sourceY);
                                int heroX = x - ANCHOR_X, heroY = y - ANCHOR_Y;
                                if (heroX >= 0 && heroX < 12 && heroY >= 0 && heroY < 15
                                        && foreground.getPixel(frame * 12 + heroX, tier * 15 + heroY) >>> 24 != 0) color = 0;
                                bitmap.setPixel(left + x, top + y, color);
                            }
                        }
                    }
                }
            }
            TextureCache.add(key, texture);
        }
    }
}
