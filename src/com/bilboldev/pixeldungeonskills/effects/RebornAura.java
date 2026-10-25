package com.bilboldev.pixeldungeonskills.effects;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import com.bilboldev.gltextures.SmartTexture;
import com.bilboldev.gltextures.TextureCache;
import com.bilboldev.noosa.Game;
import com.bilboldev.noosa.Group;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import javax.microedition.khronos.opengles.GL10;

public class RebornAura extends Group {
    private static final Object TEXTURE = RebornAura.class;
    private final CharSprite owner;
    private final Image[] parts = new Image[9];
    private float time;
    private boolean disposed;

    public RebornAura(CharSprite owner) {
        this.owner = owner;
        time = owner.ch == null ? 0 : owner.ch.pos * 0.17f;
        SmartTexture texture = texture();
        for (int i = 0; i < parts.length; i++) {
            Image part = parts[i] = new Image(texture);
            if (i < 3) {
                part.frame(0, 0, 32, 32);
                part.origin.set(16);
                part.hardlight(i == 0 ? 0xFFD06A : i == 1 ? 0xEE723B : 0xFFF1BB);
            } else {
                part.frame(0, 32, 3, 3);
                part.origin.set(1.5f);
                part.hardlight(i % 2 == 0 ? 0xFFB64D : 0xFFF1BB);
            }
            add(part);
        }
        animate();
    }

    public CharSprite owner() {
        return owner;
    }

    @Override
    public void update() {
        if (!owner.exists || owner.ch != null && owner.ch.sprite != owner) {
            killAndErase();
            return;
        }
        time += Game.elapsed;
        animate();
    }

    private void animate() {
        visible = owner.visible && owner.exists && (owner.ch == null || Dungeon.visible == null
                || owner.ch.pos >= 0 && owner.ch.pos < Dungeon.visible.length && Dungeon.visible[owner.ch.pos]);
        float x = owner.x + owner.width * 0.5f;
        float y = owner.y + owner.height * 0.55f;

        float opacity = Math.max(0, owner.am);
        for (int i = 0; i < parts.length; i++) {
            Image part = parts[i];
            if (i < 3) {
                part.x = x - part.origin.x;
                part.y = y - part.origin.y;
                part.angle = time * (i == 1 ? -48 : 62) + i * 120;
                float pulse = (float)Math.sin(time * 3 + i * 2);
                part.scale.set((Math.max(owner.width, owner.height) + 10) / 32f + pulse * 0.035f);
                part.alpha(opacity * (0.65f + pulse * 0.12f));
            } else {
                float rise = (time * 0.55f + (i - 3) / 6f) % 1;
                part.x = x - part.origin.x + (float)Math.sin(i * 2.4 + time * 0.7) * (6 + i % 2 * 2);
                part.y = y - part.origin.y + 8 - rise * (owner.height + 12);
                part.scale.set(0.45f + (1 - rise) * 0.3f);
                part.alpha(opacity * (float)Math.sin(rise * Math.PI) * 0.85f);
            }
        }
    }

    @Override
    public void draw() {
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
        super.draw();
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void killAndErase() {
        super.killAndErase();
        destroy();
    }

    @Override
    public void destroy() {
        if (disposed) return;
        disposed = true;
        super.destroy();
    }

    private static SmartTexture texture() {
        if (!TextureCache.contains(TEXTURE)) {
            int[] pixels = new int[32 * 36];
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                double dx = x - 15.5, dy = y - 15.5;
                double angle = Math.atan2(dy, dx);
                if (angle < -2.2 || angle > -0.1) continue;
                double head = (angle + 2.2) / 2.1;
                double radius = 10 + 3.5 * head;
                if (Math.abs(Math.sqrt(dx * dx + dy * dy) - radius) < 0.35 + head * 1.05) {
                    pixels[x + y * 32] = ((int)(90 + 165 * head) << 24) | 0xFFFFFF;
                }
            }
            for (int y = 0; y < 3; y++) for (int x = 0; x < 3; x++) {
                if (x == 1 || y == 1) pixels[x + (32 + y) * 32] = 0xFFFFFFFF;
            }
            Bitmap bitmap = Bitmap.createBitmap(32, 36, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, 32, 0, 0, 32, 36);
            TextureCache.add(TEXTURE, new SmartTexture(bitmap));
        }
        return TextureCache.get(TEXTURE);
    }
}
