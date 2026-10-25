package com.bilboldev.pixeldungeonskills.scenes;

import com.bilboldev.noosa.BitmapText;
import com.bilboldev.noosa.Camera;
import com.bilboldev.noosa.ColorBlock;
import com.bilboldev.noosa.Gizmo;
import com.bilboldev.noosa.Image;
import com.bilboldev.pixeldungeonskills.Dungeon;
import com.bilboldev.pixeldungeonskills.Presentation;
import com.bilboldev.pixeldungeonskills.actors.Char;
import com.bilboldev.pixeldungeonskills.effects.FloatingText;
import com.bilboldev.pixeldungeonskills.effects.EmoIcon;
import com.bilboldev.pixeldungeonskills.sprites.CharSprite;
import com.bilboldev.pixeldungeonskills.ui.HealthIndicator;
import com.bilboldev.pixeldungeonskills.ui.MobHealth;
import com.bilboldev.pixeldungeonskills.ui.MobHealthBar;

import java.util.ArrayList;

final class FirstPersonCombat {
    private final GameScene scene;
    private final FirstPersonView view;
    private final Camera ui;
    private final int width, height;
    private final float pixelX, pixelY;
    private final ColorBlock healthBackground = new ColorBlock(1, 1, 0xFFCC0000);
    private final ColorBlock healthLevel = new ColorBlock(1, 1, 0xFF00CC00);
    private final Image raisedIcon = new Image();
    private MobHealthBar healthBars;
    private final ArrayList<BitmapText> labels = new ArrayList<>();

    FirstPersonCombat(GameScene scene, FirstPersonView view, Camera ui, int width, int height) {
        this.scene = scene;
        this.view = view;
        this.ui = ui;
        this.width = width;
        this.height = height;
        pixelX = (float)ui.width / width;
        pixelY = (float)ui.height / height;
        healthBackground.camera = healthLevel.camera = ui;
        raisedIcon.camera = ui;
    }

    void draw() {
        health();
        view.drawIndicators();
        raisedIndicators();
        int used = 0;
        if (!scene.statuses.exists || !scene.statuses.visible) return;
        for (int i = 0; i < scene.statuses.length; i++) {
            Gizmo g = scene.statuses.memberAt(i);
            if (!(g instanceof FloatingText) || !g.exists || !g.visible) continue;
            FloatingText source = (FloatingText)g;
            if (source.font() == null || source.text() == null || source.text().isEmpty()) continue;
            CharSprite owner = source.owner();
            if (owner != null && owner.ch != null && owner.ch.isAlive() && !owner.visible) continue;
            FirstPersonView.StatusPoint point;
            if (!view.isThirdPerson() && owner == Dungeon.hero.sprite) {

                point = new FirstPersonView.StatusPoint(width * 0.5, height * 0.70, 64);
            } else {
                int cell = owner != null && owner.ch != null ? owner.ch.pos : source.cell();
                point = view.statusPoint(cell, source.anchorHeight(), owner);
            }
            if (point == null) continue;
            if (used == labels.size()) {
                BitmapText label = new BitmapText();
                label.camera = ui;
                labels.add(label);
            }
            BitmapText label = labels.get(used++);
            if (label.font() != source.font() || !source.text().equals(label.text())) {
                label.font(source.font());
                label.text(source.text(), false);
                label.measure();
            }

            float scale = (float)Math.max(0.8, Math.min(1.5, point.pixelsPerTile / 64));
            label.scale.set(source.scale.x * scale * pixelX, source.scale.y * scale * pixelY);
            label.x = PixelScene.align(ui, (float)(point.x * pixelX - label.width() * 0.5));
            label.y = PixelScene.align(ui, (float)((point.y - 5 - source.rise() * scale) * pixelY - label.height()));
            label.rm = source.rm;
            label.gm = source.gm;
            label.bm = source.bm;
            label.am = source.am;
            label.ra = source.ra;
            label.ga = source.ga;
            label.ba = source.ba;
            label.aa = source.aa;
            label.draw();
        }
    }

    private void raisedIndicators() {
        if (!scene.emoicons.exists || !scene.emoicons.visible) return;
        for (int i = 0; i < scene.emoicons.length; i++) {
            Gizmo member = scene.emoicons.memberAt(i);
            if (!(member instanceof EmoIcon) || !member.exists || !member.visible) continue;
            EmoIcon icon = (EmoIcon)member;
            CharSprite owner = icon.owner();
            if (icon.heightOffset <= 0 || owner == null || owner.ch == null || !owner.exists || !owner.visible) continue;
            FirstPersonView.StatusPoint point = view.healthPoint(owner.ch.pos, Math.max(0.5, owner.height / 16.0), owner);
            if (point == null || point.y > height) continue;
            float scale = (float)Math.max(0.8, Math.min(1.6, point.pixelsPerTile / 16.0));
            raisedIcon.copy(icon);
            raisedIcon.scale.set(icon.scale.x * scale * pixelX, icon.scale.y * scale * pixelY);
            raisedIcon.am = icon.am;
            raisedIcon.x = PixelScene.align(ui, (float)point.x * pixelX - raisedIcon.width() * 0.5f);

            raisedIcon.y = PixelScene.align(ui, Math.max(12,
                    ((float)point.y - icon.heightOffset * scale) * pixelY - raisedIcon.height()));
            raisedIcon.draw();
        }
    }

    private void health() {
        HealthIndicator indicator = HealthIndicator.instance;
        if (indicator == null || indicator.parent != scene || !indicator.exists || !indicator.visible) return;
        if (Presentation.reborn()) {
            rebornHealth(indicator.mobHealth());
            return;
        }
        Char target = indicator.target();
        if (target == null || !target.isAlive() || target.HT <= 0 || target.sprite == null
                || !target.sprite.exists || !target.sprite.visible) return;
        if (!view.isThirdPerson() && target == Dungeon.hero) return;
        FirstPersonView.StatusPoint point = view.statusPoint(target.pos, Math.max(0.5, target.sprite.height / 16.0), target.sprite);
        if (point == null) return;
        float w = (float)Math.max(12, Math.min(48, target.sprite.width / 16.0 * point.pixelsPerTile));
        healthBackground.x = healthLevel.x = PixelScene.align(ui, (float)(point.x - w * 0.5) * pixelX);
        healthBackground.y = healthLevel.y = PixelScene.align(ui, (float)point.y * pixelY);
        healthBackground.size(w * pixelX, 2 * pixelY);
        healthLevel.size(w * pixelX * Math.max(0, Math.min(1, (float)target.HP / target.HT)), 2 * pixelY);
        healthBackground.draw();
        healthLevel.draw();
    }

    private void rebornHealth(MobHealth health) {
        if (healthBars == null) healthBars = new MobHealthBar();
        for (int i = 0; i < health.bars().size(); i++) {
            MobHealth.State bar = health.bars().get(i);
            if (!bar.isVisible()) continue;
            CharSprite sprite = bar.deathAge < 0 ? bar.mob.sprite : null;
            FirstPersonView.StatusPoint point = view.healthPoint(bar.cell,
                    Math.max(0.5, bar.spriteHeight / 16.0), sprite);
            if (point == null) continue;
            if (point.y < 0 || point.y > height) continue;
            float scale = (float)Math.max(0.8, Math.min(1.6, point.pixelsPerTile / 16.0));
            float x = PixelScene.align(ui, (float)point.x * pixelX);
            float y = PixelScene.align(ui, (float)point.y * pixelY);
            float headroom = (y - 2) / (MobHealthBar.height(bar) * pixelY);
            scale = Math.max(0.6f, Math.min(scale, headroom));
            healthBars.draw(bar, ui, x, y, scale * pixelX, scale * pixelY,
                    bar.selected ? 1 : 0.5f);
        }
    }

    void destroy() {
        healthBackground.destroy();
        healthLevel.destroy();
        raisedIcon.destroy();
        if (healthBars != null) healthBars.destroy();
        for (BitmapText label : labels) label.destroy();
        labels.clear();
    }
}
