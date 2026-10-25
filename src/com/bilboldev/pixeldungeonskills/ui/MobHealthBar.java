package com.bilboldev.pixeldungeonskills.ui;

import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;

public final class MobHealthBar {
    private static final int ROW_HEIGHT = 4;
    private static final int STEP = ROW_HEIGHT - 1;
    private static final int FRAME = 0x19151E;
    private static final int METAL = 0x70604C;
    private static final int HIGHLIGHT = 0xC4AD7F;
    private static final int EMPTY = 0x66313B;
    private static final int HEALTH = 0x80A76A;
    private static final int HEALTH_LIGHT = 0xB2CC8C;
    private static final int ROT = 0x9BA969;
    private static final int FIRE = 0xDC9E58;

    private final ColorBlock block = new ColorBlock(1, 1, 0xFFFFFFFF);
    private float left, top, scaleX, scaleY, opacity;

    public static float width(MobHealth.State state) {
        return state.width + margin(state) * 2;
    }

    public static float height(MobHealth.State state) {
        return state.rows * STEP + 1 + margin(state) * 2;
    }

    private static int margin(MobHealth.State state) {
        return state.yog ? 4 : state.fist != 0 || state.boss ? 2 : 0;
    }

    public void draw(MobHealth.State state, Camera camera, float centerX, float bottomY,
                     float scaleX, float scaleY, float alpha) {
        if (alpha <= 0 || scaleX <= 0 || scaleY <= 0) return;
        block.camera = camera;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        opacity = alpha;
        left = centerX - width(state) * scaleX * 0.5f;
        top = bottomY - height(state) * scaleY;

        int inset = margin(state);
        if (state.deathAge < 0) {
            float shakeX = state.shakeX();
            float shakeY = state.shakeY();
            float x = inset + shakeX;
            float y = inset + shakeY;
            if (state.boss && !state.yog && state.fist == 0) {
                brackets(x, y, state.width, state.rows * STEP + 1, state.reveal(0));
            }
            if (state.yog) {
                if ((state.shields & 1) != 0) shield(state, 1, x, y);
                if ((state.shields & 2) != 0) shield(state, 2, x, y);
            } else if (state.fist != 0) {
                shield(state, state.fist, x, y);
            }
            rect(x, y, state.width, state.rows * STEP + 1, FRAME, 1);
            for (int row = 0; row < state.rows; row++) {
                row(state, row, x, y + (state.rows - row - 1) * STEP);
            }
        }

        for (int i = 0; i < state.breaks().size(); i++) {
            MobHealth.Break broken = state.breaks().get(i);
            if (broken.shield != 0) {
                shieldShards(state, broken);
            } else {
                shards(state, broken);
            }
        }
    }

    private void row(MobHealth.State state, int row, float x, float y) {
        float reveal = state.reveal(row);
        float rimAlpha = 0.3f + reveal * 0.7f;
        float width = state.width - 2;
        rect(x + 1, y + 1, width, ROW_HEIGHT - 2, EMPTY, rimAlpha);
        float fill = state.fill(row) * reveal * width;
        if (fill > 0) {
            rect(x + 1, y + 1, fill, ROW_HEIGHT - 2, HEALTH, 1);
            rect(x + 1, y + 1, fill, 0.5f, HEALTH_LIGHT, 0.7f);
            if (reveal > 0 && reveal < 1) {
                float flash = (float)Math.sin(reveal * Math.PI);
                float glint = Math.min(1, fill);
                rect(x + 1 + fill - glint, y + 1, glint, ROW_HEIGHT - 2, HIGHLIGHT, flash * 0.7f);
            }
        }
        if (state.champion || state.boss) {
            rect(x + 1, y, width, 0.5f, METAL, rimAlpha);
        }
    }

    private void brackets(float x, float y, float width, float height, float reveal) {
        float alpha = 0.25f + 0.75f * reveal;
        rect(x - 2, y, 1, height, FRAME, alpha);
        rect(x + width + 1, y, 1, height, FRAME, alpha);
        rect(x - 2, y, 2, 1, METAL, alpha);
        rect(x + width, y, 2, 1, METAL, alpha);
        rect(x - 2, y + height - 1, 2, 1, METAL, alpha);
        rect(x + width, y + height - 1, 2, 1, METAL, alpha);
        float flash = (float)Math.sin(reveal * Math.PI);
        rect(x - 2, y, 1, 1, HIGHLIGHT, flash);
        rect(x + width + 1, y, 1, 1, HIGHLIGHT, flash);
    }

    private void shield(MobHealth.State state, int shield, float x, float y) {
        int distance = state.yog && shield == 1 ? 4 : 2;
        float width = state.width + distance * 2;
        float height = state.rows * STEP + 1 + distance * 2;
        x -= distance;
        y -= distance;
        int color = shield == 1 ? ROT : FIRE;
        float reveal = state.reveal(0);
        float alpha = 0.3f + 0.7f * reveal;

        rect(x + 2, y, width - 4, 1, color, alpha);
        rect(x + 1, y + 1, 1, 1, color, alpha);
        rect(x + width - 2, y + 1, 1, 1, color, alpha);
        rect(x, y + 2, 1, height - 4, color, alpha);
        rect(x + width - 1, y + 2, 1, height - 4, color, alpha);
        rect(x + 1, y + height - 2, 1, 1, color, alpha);
        rect(x + width - 2, y + height - 2, 1, 1, color, alpha);
        rect(x + 2, y + height - 1, width - 4, 1, color, alpha * 0.65f);
        rect(x + width * 0.5f - 1, y + height - 1, 2, 1, color, alpha);
    }

    private void shards(MobHealth.State state, MobHealth.Break broken) {
        float age = broken.age;
        float fade = 1 - age / MobHealth.SHATTER_DURATION;
        if (fade <= 0) return;
        int inset = margin(state);
        float rowY = inset + (state.rows - broken.row - 1) * STEP;
        float force = state.boss ? 1.35f : 1;
        int pieces = state.boss ? 6 : 4;
        for (int piece = 0; piece < pieces; piece++) {
            float position = (float)piece / (pieces - 1);
            float x = inset + 1 + position * (state.width - 3);
            x += (position - 0.5f) * 16 * age * force;
            float y = rowY + 1 + (piece % 2) * 0.5f;
            y += (-9 - (piece % 3) * 3) * age * force + 24 * age * age;
            int color = piece % 2 == 0 ? HEALTH_LIGHT : state.boss ? HIGHLIGHT : EMPTY;
            rect(x, y, 1.25f, 1.25f, FRAME, fade);
            rect(x, y, 0.75f, 0.75f, color, fade);
        }
    }

    private void shieldShards(MobHealth.State state, MobHealth.Break broken) {
        float age = broken.age;
        float fade = 1 - age / MobHealth.SHATTER_DURATION;
        if (fade <= 0) return;
        int color = broken.shield == 1 ? ROT : FIRE;
        float halfWidth = state.width * 0.5f;
        int distance = state.yog && broken.shield == 1 ? 4 : 2;
        float center = width(state) * 0.5f;
        float middle = height(state) * 0.5f;
        for (int side = -1; side <= 1; side += 2) {
            for (int piece = 0; piece < 3; piece++) {
                float x = center + side * (halfWidth + distance - 1 + age * (6 + piece * 3));
                float y = middle + (piece - 1) * 3 - age * (12 - piece * 3) + 24 * age * age;
                rect(x, y, 1.5f, 1, color, fade);
                rect(x + side, y + 1, 1, 1, color, fade * 0.6f);
            }
        }
    }

    private void rect(float x, float y, float width, float height, int color, float alpha) {
        if (alpha <= 0 || width <= 0 || height <= 0) return;
        block.x = left + x * scaleX;
        block.y = top + y * scaleY;
        block.size(width * scaleX, height * scaleY);
        block.hardlight(color);
        block.alpha(opacity * alpha);
        block.draw();
    }

    public void destroy() {
        block.destroy();
    }
}
